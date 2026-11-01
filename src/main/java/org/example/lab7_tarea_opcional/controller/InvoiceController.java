
package org.example.lab7_tarea_opcional.controller;

import jakarta.validation.Valid;
import org.example.lab7_tarea_opcional.dto.InvoiceDetailForm;
import org.example.lab7_tarea_opcional.dto.InvoiceForm;
import org.example.lab7_tarea_opcional.entity.Invoice;
import org.example.lab7_tarea_opcional.entity.InvoiceDetail;
import org.example.lab7_tarea_opcional.entity.Product;
import org.example.lab7_tarea_opcional.respository.CustomerRepository;
import org.example.lab7_tarea_opcional.respository.InvoiceDetailRepository;
import org.example.lab7_tarea_opcional.respository.InvoiceRepository;
import org.example.lab7_tarea_opcional.respository.ProductRepository;
import org.example.lab7_tarea_opcional.service.InvoiceService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/comprobantes")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InvoiceService invoiceService;

    //Recibimos los repositorios y el servicio que necesitamos para trabajar con los comprobantes
    public InvoiceController(InvoiceRepository invoiceRepository,
                             InvoiceDetailRepository invoiceDetailRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository,
                             InvoiceService invoiceService) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.invoiceService = invoiceService;
    }

    //Mostramos todos los comprobantes y sus respectivos totales
    @GetMapping
    public String listar(Model model) {
        List<Invoice> comprobantes = invoiceRepository.findAll();
        model.addAttribute("comprobantes", comprobantes);
        model.addAttribute("totales", invoiceService.calcularTotales(comprobantes));
        return "comprobantes/lista";
    }

    //Mostramos el formulario para registrar un nuevo comprobante
    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("invoiceForm") InvoiceForm invoiceForm, Model model) {
        invoiceForm.setDate(LocalDate.now());
        cargarFormulario(invoiceForm, model);
        return "comprobantes/formulario";
    }

    //Recibimos los datos del formulario y validamos antes de guardarlos
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("invoiceForm") InvoiceForm invoiceForm,
                          BindingResult result,
                          Model model,
                          RedirectAttributes attr) {

        //Comprobamos las reglas del comprobante y agregamos los posibles errores
        invoiceService.validar(invoiceForm, result);

        //Si hay algún error, volvemos al formulario sin guardar el comprobante
        if (result.hasErrors()) {
            cargarFormulario(invoiceForm, model);
            return "comprobantes/formulario";
        }

        Invoice invoice = invoiceService.guardarComprobante(invoiceForm);
        attr.addFlashAttribute("mensaje", "Pedido registrado correctamente");
        return "redirect:/comprobantes/" + invoice.getId();
    }

    //Mostramos los datos y detalles de un comprobante
    @GetMapping("/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        Invoice invoice = invoiceRepository.findById(id).orElse(null);

        if (invoice == null) {
            return "redirect:/comprobantes";
        }

        List<InvoiceDetail> detalles = invoiceDetailRepository.findByInvoiceId(id);
        model.addAttribute("comprobante", invoice);
        model.addAttribute("detalles", detalles);
        model.addAttribute("total", invoiceService.calcularTotal(detalles));
        return "comprobantes/detalle";
    }

    //Cargamos los clientes y productos para mostrarlos en el formulario
    private void cargarFormulario(InvoiceForm form, Model model) {
        List<Product> productos = productRepository.findAll(Sort.by("id"));

        //Agregamos una fila por cada producto del catálogo
        while (form.getDetails().size() < productos.size()) {
            form.getDetails().add(new InvoiceDetailForm());
        }

        //Asignamos el ID de cada producto a su respectiva fila
        for (int i = 0; i < productos.size(); i++) {
            if (form.getDetails().get(i).getProductId() == null) {
                form.getDetails().get(i).setProductId(productos.get(i).getId());
            }
        }

        model.addAttribute("clientes", customerRepository.findAll());
        model.addAttribute("productos", productos);
    }
}
