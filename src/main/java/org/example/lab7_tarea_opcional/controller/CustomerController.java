package org.example.lab7_tarea_opcional.controller;


import jakarta.validation.Valid;
import org.example.lab7_tarea_opcional.entity.Customer;
import org.example.lab7_tarea_opcional.respository.CustomerRepository;
import org.example.lab7_tarea_opcional.service.CustomerService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public CustomerController(CustomerRepository customerRepository,
                              CustomerService customerService) {
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", customerRepository.findAll());
        return "clientes/lista";
    }


    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("customer") Customer customer) {
        return "clientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("customer") Customer customer,
                          BindingResult result) {


        //Comprobamos que los campos "document" y "documentType" no tengan errores de validación anteriores
        if (!result.hasFieldErrors("document") && !result.hasFieldErrors("documentType")) {


            //Comprobamos si el documento corresponde al tipo seleccionado.
            if (!customerService.documentoValidoParaTipo(customer.getDocument(), customer.getDocumentType())) {
                result.rejectValue("document", "documento.invalido",
                        "El DNI debe tener 8 dígitos y el RUC 11 dígitos");

            }

            //Si el documento tiene el formato correcto, comprobamos si ya está registrado por otro cliente
            else if (customerService.documentoDuplicado(customer.getDocument(), customer.getId())) {
                result.rejectValue("document", "documento.duplicado",
                        "Ya existe un cliente registrado con ese número de documento");
            }
        }

        //Comprobamos si existe algún error en las validaciones
        if (result.hasErrors()) {
            return "clientes/formulario";
        }

        customerService.guardar(customer);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {

        //Buscamos al cliente en la base de datos utilizando su ID
        Customer customer = customerRepository.findById(id).orElse(null);

        //Comprobamos si no encontramos ningún cliente con ese ID
        if (customer == null) {
            return "redirect:/clientes";
        }

        model.addAttribute("customer", customer);
        return "clientes/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes attr) {

        try {

            //Comprobamos si existe un cliente con ese ID en la base de datos
            if (customerRepository.existsById(id)) {
                customerRepository.deleteById(id);
            }

        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error",
                    "No se puede eliminar: el cliente tiene pedidos registrados");
        }
        return "redirect:/clientes";
    }

}
