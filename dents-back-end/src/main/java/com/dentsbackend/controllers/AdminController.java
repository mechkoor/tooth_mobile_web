package com.dentsbackend.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.dentsbackend.repositories.PWRepository;

import com.dentsbackend.entities.Groupe;
import com.dentsbackend.entities.PW;
import com.dentsbackend.entities.Professor;
import com.dentsbackend.entities.Role;
import com.dentsbackend.entities.Student;
import com.dentsbackend.entities.StudentPW;
import com.dentsbackend.entities.StudentPWPK;
import com.dentsbackend.entities.Tooth;
import com.dentsbackend.repositories.GroupeRepository;
import com.dentsbackend.repositories.ProfessorRepository;
import com.dentsbackend.repositories.RoleRepository;
import com.dentsbackend.repositories.StudentPWRepository;
import com.dentsbackend.repositories.StudentRepository;
import com.dentsbackend.repositories.ToothRepository;
import com.dentsbackend.services.CustomUserDetails;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/prof")
public class AdminController {

  @Autowired
  ProfessorRepository professorRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  ToothRepository ToothRepository;

  @Autowired
  PWRepository PWRepository;

  @Autowired
  StudentPWRepository spr;

  // ...............................................gestion du
  // profile...............................................
  @GetMapping
  public String prof(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      int groupes = groupeRepository.findByProfessor(prof1).size();
      model.addAttribute("groupes", groupes);
      List<Student> students = new ArrayList<>();
      for (Groupe groupe : groupeRepository.findByProfessor(prof1)) {
        students.addAll(studentRepository.findByGroupe(groupe));
      }
      int nbr = students.size();
      model.addAttribute("students", nbr);
      int pws = PWRepository.findAll().size();
      model.addAttribute("pws", pws);
      int dents = ToothRepository.findAll().size();
      model.addAttribute("dents", dents);

      List<String> titles = new ArrayList<>();
      List<Integer> nbrgr = new ArrayList<>();
      List<Integer> nbrtp = new ArrayList<>();

      for (Groupe groupe : groupeRepository.findByProfessor(prof1)) {
        titles.add(groupe.getCode());
        nbrgr.add(studentRepository.findByGroupe(groupe).size());
      }

      for (Groupe groupe : groupeRepository.findByProfessor(prof1)) {
        nbrtp.add(PWRepository.findByGroupes(groupe).size());
      }
      model.addAttribute("nbrtp", nbrtp);
      model.addAttribute("titles", titles);
      model.addAttribute("nbrgr", nbrgr);

      model.addAttribute("professor", prof1);

    }
    return "prof";

  }

  @GetMapping("/profile")
  public String profile(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      model.addAttribute("userdetail", userDetails);
      model.addAttribute("professor", prof1);

    }
    return "profile";

  }

  @PostMapping("/update/{id}")
  public String updateProfessor(@PathVariable("id") long id, Professor professor, Model model,
      @RequestParam("file") MultipartFile photoFile) {
    Optional<Role> role = roleRepository.findByName("ROLE_PROFESSOR");
    Role roole = role.get();
    professor.setRoles(Set.of(roole));

    professor.setId(id);
    String a = passwordEncoder.encode(professor.getPassword());
    professor.setPassword(a);
    if (photoFile != null && !photoFile.isEmpty()) {
      try {
        byte[] photoBytes = photoFile.getBytes();
        professor.setPhoto(photoBytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    professorRepository.save(professor);

    return "redirect:/prof";
  }

  // ...............................................gestion du
  // groupe...............................................

  @Autowired
  GroupeRepository groupeRepository;

  @GetMapping("/groupe")
  public String groupe(Groupe groupe, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      Set<String> years = new HashSet<>();
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      for (Groupe gr : groupes) {
        years.add(gr.getYear());
      }
      model.addAttribute("years", years);
      model.addAttribute("groupes", groupes);
      model.addAttribute("professor", prof1);

    }
    return "groupe";
  }

  @GetMapping("/addg")
  public String showAddForm(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();

      model.addAttribute("groupes", groupeRepository.findAll());
      model.addAttribute("professor", prof1);

      model.addAttribute("mode", "add");
      model.addAttribute("groupe", new Groupe());

    }
    return "groupe";

  }

  @PostMapping("/addgroupe")
  public String savegroupe(Groupe groupe, Model model, Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Optional<Professor> prof = professorRepository.findById(userDetails.getId());

    Professor prof1 = prof.get();
    System.out.println(prof1.getEmail());

    groupe.setProfessor(prof1);
    groupeRepository.save(groupe);
    return "redirect:/prof/groupe";
  }

  @GetMapping("/editg/{id}")
  public String editProfesssor(@PathVariable("id") long id, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
    }

    Groupe groupe = groupeRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

    model.addAttribute("groupes", groupeRepository.findAll());

    model.addAttribute("groupe", groupe);
    model.addAttribute("mode", "update");

    return "groupe";
  }

  @PostMapping("/updategroupe/{id}")
  public String updateProfessor(@PathVariable("id") long id, Groupe groupe, Model model,
      Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Optional<Professor> prof = professorRepository.findById(userDetails.getId());
    Professor prof1 = prof.get();

    groupe.setId(id);
    groupe.setProfessor(prof1);

    groupeRepository.save(groupe);

    return "redirect:/prof/groupe";
  }

  @GetMapping("/deletegroupe/{id}")
  public String deleteProfessor(@PathVariable("id") long id, Model model, Authentication authentication) {
    try {
      if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<Professor> prof = professorRepository.findById(userDetails.getId());
        Professor prof1 = prof.get();
        model.addAttribute("professor", prof1);
      }

      Groupe group = groupeRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
      groupeRepository.delete(group);

      return "redirect:/prof/groupe";
    } catch (Exception e) {
      e.printStackTrace();
      return "error500";
    }
  }

  @GetMapping("/groupeByYear")
  public String groupeByYear(@RequestParam("year") String year, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }

      Set<String> years = new HashSet<>();
      List<Groupe> groupees = groupeRepository.findByProfessor(prof1);
      for (Groupe gr : groupees) {
        years.add(gr.getYear());
      }
      model.addAttribute("years", years);

      List<Groupe> groupes = groupeRepository.findByYear(year);
      model.addAttribute("groupes", groupes);
    }

    return "groupe";
  }

  // ...............................................gestion des
  // etudiants...............................................

  @Autowired
  StudentRepository studentRepository;

  @GetMapping("/etudiant")
  public String etudiant(Student student, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      model.addAttribute("professor", prof1);
      List<Student> students = new ArrayList<>();
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      for (Groupe gr : groupes) {
        List<Student> studentss = studentRepository.findByGroupe(gr);
        students.addAll(studentss);
      }

      List<String> encodedPhotos = new ArrayList<>();
      for (Student stu : students) {
        byte[] photo1 = stu.getPhoto();
        if (photo1 != null && photo1.length > 0) {
          String encodedPhot = Base64.getEncoder().encodeToString(photo1);
          encodedPhotos.add(encodedPhot);
        } else {
          encodedPhotos.add("");
        }
      }
      model.addAttribute("groupes", groupes);
      model.addAttribute("students", students);
      model.addAttribute("encodedPhotos", encodedPhotos);

    }
    return "etudiant";
  }

  @GetMapping("/adde")
  public String showAddetudiant(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      List<Student> students = new ArrayList<>();
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      for (Groupe gr : groupes) {
        List<Student> studentss = studentRepository.findByGroupe(gr);
        students.addAll(studentss);
      }
      List<String> encodedPhotos = new ArrayList<>();
      for (Student stu : students) {
        byte[] photo1 = stu.getPhoto();
        if (photo1 != null && photo1.length > 0) {
          String encodedPhot = Base64.getEncoder().encodeToString(photo1);
          encodedPhotos.add(encodedPhot);
        } else {
          encodedPhotos.add("");
        }
      }
      model.addAttribute("students", students);
      model.addAttribute("encodedPhotos", encodedPhotos);

      model.addAttribute("professor", prof1);
      model.addAttribute("mode", "add");
      model.addAttribute("student", new Student());
      model.addAttribute("groupes", groupeRepository.findByProfessor(prof1));

    }
    return "etudiant";

  }

  @PostMapping("/addetudiant")
  public String save(Student student, Model model, @RequestParam("file") MultipartFile photoFile) {

    if (photoFile != null && !photoFile.isEmpty()) {
      try {
        byte[] photoBytes = photoFile.getBytes();
        student.setPhoto(photoBytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    studentRepository.save(student);

    return "redirect:/prof/etudiant";
  }

  @GetMapping("/edite/{id}")
  public String editetudiant(@PathVariable("id") long id, Model model, Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Optional<Professor> prof = professorRepository.findById(userDetails.getId());
    Professor prof1 = prof.get();
    model.addAttribute("professor", prof1);

    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

    if (student.getPhoto() != null) {
      String encodedPhot = Base64.getEncoder().encodeToString(student.getPhoto());
      model.addAttribute("encodedPhoto", encodedPhot);

    }
    List<Student> students = studentRepository.findAll();
    List<String> encodedPhotos = new ArrayList<>();
    for (Student stu : students) {
      byte[] photo1 = stu.getPhoto();
      if (photo1 != null && photo1.length > 0) {
        String encodedPhot = Base64.getEncoder().encodeToString(photo1);
        encodedPhotos.add(encodedPhot);
      } else {
        encodedPhotos.add("");
      }
    }
    model.addAttribute("students", students);
    model.addAttribute("encodedPhotos", encodedPhotos);
    model.addAttribute("student", student);
    model.addAttribute("mode", "update");
    model.addAttribute("groupes", groupeRepository.findByProfessor(prof1));

    return "etudiant";
  }

  @PostMapping("/updateetudiant/{id}")
  public String updateedutiant(@PathVariable("id") long id, Student student, Model model,
      @RequestParam("file") MultipartFile photoFile, @RequestParam("groupe") Groupe groupe) {
    Groupe grp = groupeRepository.findById(groupe.getId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

    if (grp != null) {
      student.setGroupe(grp);
    }
    if (photoFile != null && !photoFile.isEmpty()) {
      try {
        byte[] photoBytes = photoFile.getBytes();
        student.setPhoto(photoBytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    studentRepository.save(student);

    return "redirect:/prof/etudiant";
  }

  @GetMapping("/deleteetudiant/{id}")
  public String deleteEtudiant(@PathVariable("id") long id, Model model, Authentication authentication) {
    try {
      if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<Professor> prof = professorRepository.findById(userDetails.getId());
        Professor prof1 = prof.get();
        model.addAttribute("professor", prof1);
      }

      Student student = studentRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
      studentRepository.delete(student);

      return "redirect:/prof/etudiant";
    } catch (Exception e) {
      e.printStackTrace();
      return "error500";
    }
  }

  @GetMapping("/studentByGroupe")
  public String studentByGroupe(@RequestParam("groupe") Groupe groupe, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      model.addAttribute("groupes", groupes);
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
    }

    List<Student> students = studentRepository.findByGroupe(groupe);
    List<String> encodedPhotos = new ArrayList<>();
    for (Student stu : students) {
      byte[] photo1 = stu.getPhoto();
      if (photo1 != null && photo1.length > 0) {
        String encodedPhot = Base64.getEncoder().encodeToString(photo1);
        encodedPhotos.add(encodedPhot);
      } else {
        encodedPhotos.add("");
      }
    }

    model.addAttribute("encodedPhotos", encodedPhotos);
    model.addAttribute("students", students);
    return "etudiant";
  }

  @GetMapping("/studentofGroupe")
  public String studentOfGroupe(@RequestParam("groupe") Groupe groupe, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      model.addAttribute("groupes", groupes);
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
    }

    List<Student> students = studentRepository.findByGroupe(groupe);
    List<String> encodedPhotos = new ArrayList<>();
    for (Student stu : students) {
      byte[] photo1 = stu.getPhoto();
      if (photo1 != null && photo1.length > 0) {
        String encodedPhot = Base64.getEncoder().encodeToString(photo1);
        encodedPhotos.add(encodedPhot);
      } else {
        encodedPhotos.add("");
      }
    }

    model.addAttribute("encodedPhotos", encodedPhotos);
    model.addAttribute("students", students);
    return "groupestudents";
  }

  // ...............................................gestion des
  // dents...............................................

  @GetMapping("/dent")
  public String dent(Tooth tooth, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      List<Tooth> Tooths = ToothRepository.findAll();
      model.addAttribute("tooths", Tooths);
      model.addAttribute("professor", prof1);

    }
    return "dent";
  }

  @GetMapping("/addd")
  public String showAddDentForm(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();

      List<Tooth> Tooths = ToothRepository.findAll();
      model.addAttribute("tooths", Tooths);
      model.addAttribute("professor", prof1);

      model.addAttribute("mode", "add");
      model.addAttribute("tooth", new Tooth());

    }
    return "dent";

  }

  @PostMapping("/adddent")
  public String savedent(Tooth tooth, Model model, Authentication authentication) {
    ToothRepository.save(tooth);
    return "redirect:/prof/dent";
  }

  @GetMapping("/editd/{id}")
  public String editdent(@PathVariable("id") long id, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
    }

    Tooth tooth = ToothRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invalid Tooth Id:" + id));

    model.addAttribute("tooths", ToothRepository.findAll());

    model.addAttribute("tooth", tooth);
    model.addAttribute("mode", "update");

    return "dent";
  }

  @PostMapping("/updatedent/{id}")
  public String updatedent(@PathVariable("id") long id, Tooth tooth, Model model,
      Authentication authentication) {
    Tooth toth = ToothRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invalid tooth Id:" + id));

    if (tooth != null) {
      toth.setName(tooth.getName());
    }
    ToothRepository.save(toth);

    return "redirect:/prof/dent";
  }

  @GetMapping("/deletedent/{id}")
  public String deletedent(@PathVariable("id") long id, Model model, Authentication authentication) {
    try {
      if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<Professor> prof = professorRepository.findById(userDetails.getId());
        Professor prof1 = prof.get();
        model.addAttribute("professor", prof1);
      }

      Tooth tooth = ToothRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid Tooth Id:" + id));
      ToothRepository.delete(tooth);

      return "redirect:/prof/dent";
    } catch (Exception e) {
      e.printStackTrace();
      return "error500";
    }
  }

  // ...............................................gestion des travaux
  // pratiques...............................................

  @GetMapping("/pw")
  public String tp(PW pw, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      model.addAttribute("groupes", groupes);
      List<PW> pws = PWRepository.findAll();
      model.addAttribute("pws", pws);

    }
    return "PW";
  }

  @GetMapping("/pwByGroupe")
  public String pwByGroupe(@RequestParam("groupe") Groupe groupe, Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      model.addAttribute("groupes", groupes);
      byte[] photo = prof1.getPhoto();

      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
    }

    List<PW> pws = PWRepository.findByGroupes(groupe);
    model.addAttribute("pws", pws);
    return "PW";
  }

  @GetMapping("/addp")
  public String showAddTp(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      List<PW> pws = new ArrayList<>();
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      for (Groupe gr : groupes) {
        List<PW> pwss = PWRepository.findByGroupes(gr);
        pws.addAll(pwss);
      }
      List<Tooth> tooths = ToothRepository.findAll();
      model.addAttribute("tooths", tooths);
      model.addAttribute("pws", pws);
      model.addAttribute("professor", prof1);
      model.addAttribute("mode", "add");
      model.addAttribute("pw", new PW());

    }
    return "PW";

  }

  @PostMapping("/addpw")
  public String saveTP(PW pw, Model model) {
    PWRepository.save(pw);

    return "redirect:/prof/pw";
  }

  @GetMapping("/editp/{id}")
  public String editTP(@PathVariable("id") long id, Model model, Authentication authentication) {
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    Optional<Professor> prof = professorRepository.findById(userDetails.getId());
    Professor prof1 = prof.get();
    model.addAttribute("professor", prof1);

    PW pw = PWRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    List<Tooth> tooths = ToothRepository.findAll();
    List<PW> pws = new ArrayList<>();
    List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
    for (Groupe gr : groupes) {
      List<PW> pwss = PWRepository.findByGroupes(gr);
      pws.addAll(pwss);
    }
    model.addAttribute("pws", pws);
    model.addAttribute("pw", pw);
    model.addAttribute("tooths", tooths);
    model.addAttribute("mode", "update");
    return "PW";
  }

  @PostMapping("/updatepw/{id}")
  public String updateTP(@PathVariable("id") long id, PW pw, Model model, @RequestParam("tooth") Tooth tooth) {
    Tooth toth = ToothRepository.findById(tooth.getId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid Tooth Id:" + id));

    if (toth != null) {
      pw.setTooth(toth);
      ;
    }
    PWRepository.save(pw);

    return "redirect:/prof/pw";
  }

  @GetMapping("/deletepw/{id}")
  public String deleteTp(@PathVariable("id") long id, Model model, Authentication authentication) {
    try {
      if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Optional<Professor> prof = professorRepository.findById(userDetails.getId());
        Professor prof1 = prof.get();
        model.addAttribute("professor", prof1);
      }

      PW pw = PWRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
      PWRepository.delete(pw);

      return "redirect:/prof/pw";
    } catch (Exception e) {
      e.printStackTrace();
      return "error500";
    }
  }

  // ...............................................gestion du tp avec
  // students...............................................

  @GetMapping("/studentpw")
  public String getPw(Authentication authentication, Model model) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
        model.addAttribute("groupes", groupes);

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        for (Groupe grp : groupes) {
          List<Student> students = studentRepository.findByGroupe(grp);

          for (Student student : students) {
            List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
            allStudentPWs.addAll(studentPWs);
          }
        }
        model.addAttribute("studentPWs", allStudentPWs);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }
      }
    }

    return "studentpw";
  }

  @GetMapping("/valider/{studentId}/{pwId}")
  public String editPw(Authentication authentication, Model model, @PathVariable("pwId") Long pwid,
      @PathVariable("studentId") Long studentid) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      List<String> pwencodedPhotos1 = new ArrayList<>();
      List<String> pwencodedPhotos2 = new ArrayList<>();
      List<String> pwencodedPhotos3 = new ArrayList<>();
      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);

        List<StudentPW> allStudentPWs = new ArrayList<>();

        for (Groupe grp : groupes) {
          List<Student> students = studentRepository.findByGroupe(grp);

          for (Student student : students) {
            List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
            allStudentPWs.addAll(studentPWs);
          }
        }
        model.addAttribute("studentPWs", allStudentPWs);

        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }

      }
      model.addAttribute("studentId", studentid);
      model.addAttribute("pwId", pwid);
      model.addAttribute("mode", "valider");
    }
    return "studentpw";
  }

  @GetMapping("/valider1/{studentId}/{pwId}")
  public String editPw1(Authentication authentication, Model model, @PathVariable("pwId") Long pwid,
      @PathVariable("studentId") Long studentid) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      List<String> pwencodedPhotos1 = new ArrayList<>();
      List<String> pwencodedPhotos2 = new ArrayList<>();
      List<String> pwencodedPhotos3 = new ArrayList<>();
      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);

        List<StudentPW> allStudentPWs = new ArrayList<>();

        for (Groupe grp : groupes) {
          List<Student> students = studentRepository.findByGroupe(grp);

          for (Student student : students) {
            List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
            allStudentPWs.addAll(studentPWs);
          }
        }
        model.addAttribute("studentPWs", allStudentPWs);

        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }

      }
      model.addAttribute("studentId", studentid);
      model.addAttribute("pwId", pwid);
      model.addAttribute("mode", "valider");
    }
    return "studentDetails";
  }

  @PostMapping("/validerremarque/{id1}/{id2}")
  public String ajouterStudentPW(Authentication authentication, Model model, @PathVariable("id1") Long studentId,
      @PathVariable("id2") Long pwId, @RequestParam("remarque") String remarque) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
        model.addAttribute("groupes", groupes);

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        for (Groupe grp : groupes) {
          List<Student> students = studentRepository.findByGroupe(grp);

          for (Student student : students) {
            List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
            allStudentPWs.addAll(studentPWs);
          }
        }
        List<String> names = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          names.add(stpw.getPw().getTitle());

        }
        List<Integer> nbrs = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          nbrs.add(stpw.getNote());

        }
        System.out.println(names);
        System.out.println(nbrs);
        model.addAttribute("names", names);
        model.addAttribute("nbrs", nbrs);
        model.addAttribute("studentPWs", allStudentPWs);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }

      }

      StudentPWPK studentPWPK = new StudentPWPK();
      studentPWPK.setStudent_id(studentId);
      studentPWPK.setPw_id(pwId);
      StudentPW studentpw = spr.getById(studentPWPK);
      studentpw.setRemarque(remarque);
      studentpw.setId(studentPWPK);
      spr.save(studentpw);
    }
    return "studentpw";

  }

  @PostMapping("/validertp/{id1}/{id2}")
  public String ajouterStudenttp(Authentication authentication, Model model, @PathVariable("id1") Long studentId,
      @PathVariable("id2") Long pwId, @RequestParam("remarque") String remarque) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        for (Groupe grp : groupes) {
          List<Student> students = studentRepository.findByGroupe(grp);

          for (Student student : students) {
            List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
            allStudentPWs.addAll(studentPWs);
          }
        }
        List<String> names = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          names.add(stpw.getPw().getTitle());

        }
        List<Integer> nbrs = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          nbrs.add(stpw.getNote());

        }
        System.out.println(names);
        System.out.println(nbrs);
        model.addAttribute("names", names);
        model.addAttribute("nbrs", nbrs);
        model.addAttribute("studentPWs", allStudentPWs);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }

      }

      StudentPWPK studentPWPK = new StudentPWPK();
      studentPWPK.setStudent_id(studentId);
      studentPWPK.setPw_id(pwId);
      StudentPW studentpw = spr.getById(studentPWPK);
      studentpw.setRemarque(remarque);
      studentpw.setId(studentPWPK);
      spr.save(studentpw);
    }
    return "studentDetails";

  }

  List<Student> students = new ArrayList<>();

  @GetMapping("/getetudiants")
  public String getbygroupe(Authentication authentication, Model model, @RequestParam("id") Long id) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
        model.addAttribute("groupes", groupes);

        Optional<Groupe> groupeoptinal = groupeRepository.findById(id);
        Groupe groupe = groupeoptinal.get();

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        students = studentRepository.findByGroupe(groupe);
        model.addAttribute("students", students);

        for (Student student : students) {
          List<StudentPW> studentPWs = spr.findByStudentId(student.getId());
          allStudentPWs.addAll(studentPWs);
        }

        model.addAttribute("studentPWs", allStudentPWs);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }
      }
    }

    return "studentpw";

  }

  @GetMapping("/gettpetudiants")
  public String getbygroupeetudiant(Authentication authentication, Model model, @RequestParam("id") Long id) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
        model.addAttribute("groupes", groupes);

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        List<StudentPW> studentPWs = spr.findByStudentId(id);
        allStudentPWs.addAll(studentPWs);

        model.addAttribute("studentPWs", allStudentPWs);
        model.addAttribute("students", students);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }
      }
    }

    return "studentpw";

  }

  @GetMapping("/gettpstudent")
  public String gettpstudent(Authentication authentication, Model model, @RequestParam("id") Long id) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());

      if (prof.isPresent()) {
        Professor prof1 = prof.get();
        byte[] photo = prof1.getPhoto();
        if (photo != null) {
          String encodedPhoto = Base64.getEncoder().encodeToString(photo);
          model.addAttribute("photo", encodedPhoto);
        }
        model.addAttribute("professor", prof1);

        List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
        model.addAttribute("groupes", groupes);

        List<StudentPW> allStudentPWs = new ArrayList<>();
        List<String> pwencodedPhotos1 = new ArrayList<>();
        List<String> pwencodedPhotos2 = new ArrayList<>();
        List<String> pwencodedPhotos3 = new ArrayList<>();

        List<StudentPW> studentPWs = spr.findByStudentId(id);
        allStudentPWs.addAll(studentPWs);
        List<String> names = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          names.add(stpw.getPw().getTitle());

        }
        List<Integer> nbrs = new ArrayList<>();
        for (StudentPW stpw : allStudentPWs) {
          nbrs.add(stpw.getNote());

        }
        System.out.println(names);
        System.out.println(nbrs);
        model.addAttribute("names", names);
        model.addAttribute("nbrs", nbrs);

        model.addAttribute("studentPWs", allStudentPWs);
        model.addAttribute("students", students);
        for (StudentPW spw : allStudentPWs) {
          byte[] photo1 = spw.getImage1();
          byte[] photo2 = spw.getImage2();
          byte[] photo3 = spw.getImage3();

          if ((photo1 != null && photo1.length > 0) || (photo2 != null && photo2.length > 0)
              || (photo3 != null && photo3.length > 0)) {
            String encodedPhot1 = Base64.getEncoder().encodeToString(photo1);
            pwencodedPhotos1.add(encodedPhot1);
            model.addAttribute("photo1", pwencodedPhotos1);
            String encodedPhot2 = Base64.getEncoder().encodeToString(photo2);
            pwencodedPhotos2.add(encodedPhot2);
            model.addAttribute("photo2", pwencodedPhotos2);
            String encodedPhot3 = Base64.getEncoder().encodeToString(photo3);
            pwencodedPhotos3.add(encodedPhot3);
            model.addAttribute("photo3", pwencodedPhotos3);
          }
        }
      }
    }

    return "studentDetails";

  }

  // ...............................................gestion du tp avec les
  // groupes...............................................

  @GetMapping("/tpgroupe")
  public String tpgroupe(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }

      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      List<PW> pws = new ArrayList<>();
      for (Groupe g : groupes) {
        List<PW> pwws = PWRepository.findByGroupes(g);
        pws.addAll(pwws);

      }
      model.addAttribute("pws", pws);
    }

    return "tpgroupe";

  }

  @GetMapping("/showaffectation")
  public String showaffectation(Model model, Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      model.addAttribute("professor", prof1);
      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      List<PW> pws = PWRepository.findAll();
      model.addAttribute("pws", pws);
      model.addAttribute("groupes", groupes);
      model.addAttribute("mode", "affect");
    }

    return "PW";
  }

  @PostMapping("/affecter")
  public String affecter(@RequestParam("groupe") Long groupe, @RequestParam("pw") Long pw,
      Authentication authentication, Model model) {
    if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      Optional<Professor> prof = professorRepository.findById(userDetails.getId());
      Professor prof1 = prof.get();
      byte[] photo = prof1.getPhoto();
      if (photo != null) {
        String encodedPhoto = Base64.getEncoder().encodeToString(photo);
        model.addAttribute("photo", encodedPhoto);
      }
      model.addAttribute("professor", prof1);

      Optional<Groupe> groupee = groupeRepository.findById(groupe);
      Groupe groupeee = groupee.get();
      Optional<PW> pwww = PWRepository.findById(pw);
      PW pww = pwww.get();
      if (!pww.getGroupes().contains(groupeee)) {
        pww.getGroupes().add(groupeee);
        groupeee.getPws().add(pww);
        PWRepository.save(pww);
        model.addAttribute("success", "Tp est  affecté avec succès");
      } else {
        model.addAttribute("error", "Ce Tp est deja affecté à ce groupe");
      }

      List<Groupe> groupes = groupeRepository.findByProfessor(prof1);
      List<PW> pwss = PWRepository.findAll();
      model.addAttribute("pws", pwss);

      model.addAttribute("groupes", groupes);

    }
    return "PW";
  }

}
