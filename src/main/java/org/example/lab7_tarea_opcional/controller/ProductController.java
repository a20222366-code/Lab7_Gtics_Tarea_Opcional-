package org.example.lab7_tarea_opcional.controller;

import jakarta.validation.Valid;
import org.example.lab7_tarea_opcional.entity.Product;
import org.example.lab7_tarea_opcional.respository.ProductRepository;
import org.example.lab7_tarea_opcional.service.ProductService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//La alternativa implementada para los errores de conversión es messages.properties
@Controller
@RequestMapping("/productos")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ProductController(ProductRepository productRepository,
                             ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productRepository.findAll());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@ModelAttribute("product") Product product) {
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("product") Product product,
                          BindingResult result) {

        //Evaluamos que el nombre no sea duplicado
        if (!result.hasFieldErrors("name")
                && productService.nombreDuplicado(product.getName().trim(), product.getId())) {
            result.rejectValue("name", "nombre.duplicado",
                    "Ya existe un producto con ese nombre");
        }

        //En caso de haber error, volvemos al formulario sin guardar
        if (result.hasErrors()) {
            return "productos/formulario";
        }

        product.setName(product.getName().trim());
        productService.guardar(product);
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/productos";
        }
        model.addAttribute("product", product);
        return "productos/formulario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes attr) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
            }
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error",
                    "No se puede eliminar: el producto ya forma parte de un pedido");
        }
        return "redirect:/productos";
    }
}
