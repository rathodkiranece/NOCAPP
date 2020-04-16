package com.cognizant.noc.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cognizant.noc.entity.Admin;
import com.cognizant.noc.entity.User;
import com.cognizant.noc.repository.NOCRepository;
import com.cognizant.noc.util.GeneratePdfReport;


@Controller
@RequestMapping("/noc/")
public class NOCController {

	private final NOCRepository NOCRepository;

	private static String UPLOADED_FOLDER = "//Users//787497//Desktop//Call For Code";

	@Autowired
	public NOCController(NOCRepository NOCRepository) {
		this.NOCRepository = NOCRepository;
	}

	@GetMapping("application")
	public String showForm(User user) {
		return "noc-application-form";
	}

	@GetMapping("checkStatus")
	public String checkStatusForm(User user) {
		return "check-status";
	}
	
	@GetMapping()
	public String home( ) {
		return "home";
	}

	@GetMapping("list1")
	public String showUpdateForm(Model model) {
		model.addAttribute("users", NOCRepository.findAll());
		return "index";
	}

	@GetMapping("list")
	public String showApplicationForm(Model model) {
		model.addAttribute("users", NOCRepository.findAll());
		return "application-list";
	}
	@PostMapping("add")
	public String saveApplication(@Valid User user, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "noc-application-form";
		}
		user.setStatus("New");
		user.setDiscription("");
		user.setCreateddate(new Date().toString());
		user.setStatusDate(new Date().toString());
		
		NOCRepository.save(user);
		return "redirect:list";
	}

	@GetMapping("edit/{id}")
	public String showUpdateForm(@PathVariable("id") long id, Model model) {
		User user = NOCRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
		model.addAttribute("user", user);
		return "update-noc-application-form";
	}

	@PostMapping("update/{id}")
	public String updateForm(@PathVariable("id") long id, @Valid User user, BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			user.setId(id);
			return "update-noc-application-form";
		}

		NOCRepository.save(user);
		model.addAttribute("users", NOCRepository.findAll());
		return "index";
	}

	@GetMapping("delete/{id}")
	public String deleteForm(@PathVariable("id") long id, Model model) {
		User user = NOCRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));
		NOCRepository.delete(user);
		model.addAttribute("users", NOCRepository.findAll());
		return "index";
	}

	

	@GetMapping("login")
	public String login( ) {
		return "login";
	}

	@PostMapping("viewApplications")
	public String viewApplications(Admin admin, Model model) {
		if(admin.getCity().equalsIgnoreCase("HYD") && admin.getPassword().equalsIgnoreCase("HYD123"))
			return "redirect:list";
		else
		return "redirect:login";
	}


	@PostMapping("/upload")
	public String singleFileUpload(@RequestParam("file") MultipartFile file,
								   RedirectAttributes redirectAttributes) {

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
			return "redirect:uploadStatus";
		}

		try {

			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);

			redirectAttributes.addFlashAttribute("message",
					"You successfully uploaded '" + file.getOriginalFilename() + "'");

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "redirect:/uploadStatus";
	}

	@GetMapping("/uploadStatus")
	public String uploadStatus() {
		return "uploadStatus";
	}

	@RequestMapping("/upload")
	public String uploading(Model model) {
		File file = new File(UPLOADED_FOLDER);
		model.addAttribute("files", file.listFiles());
		return "upload-docs";
	}

	@RequestMapping(value = "/uploading", method = RequestMethod.POST)
	public String uploadingPost(@RequestParam("uploadingFiles") MultipartFile[] uploadingFiles) throws IOException {
		for(MultipartFile uploadedFile : uploadingFiles) {
			File file = new File(UPLOADED_FOLDER + uploadedFile.getOriginalFilename());
			uploadedFile.transferTo(file);
		}

		return "redirect:upload";
	}
	
	@GetMapping("/previewReport/{phoneNo}/{createddate}")
    public String UserReport(@PathVariable("phoneNo") String phoneNo, @PathVariable("createddate") String createddate, @Valid User user, BindingResult result,
			Model model) {
		System.out.println("createddate :: "+createddate);
		long mobileNo = Long.parseLong(phoneNo);
        List<User> users = (List<User>) NOCRepository.findByphoneNo(mobileNo);
        List<User> users2 = new ArrayList<>();
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
        		User next = iterator.next();
        		if(next.getCreateddate().equalsIgnoreCase(createddate)) {
        			model.addAttribute("user", next);
        			return "previewReport";
        		}
		}
        
        return "check-status-Approval";
    }
	@GetMapping("/submitCheckStatus")
	public String checkStatusApproval(@RequestParam("phoneNo") String phoneNo, Model model) {
		System.out.println("phone number   ::   "+phoneNo);
		long mobileNo = Long.parseLong(phoneNo);
		List<User> user = (List<User>) NOCRepository.findByphoneNo(mobileNo);	
		model.addAttribute("users", user);
		return "check-status-Approval";
	}
	
}
