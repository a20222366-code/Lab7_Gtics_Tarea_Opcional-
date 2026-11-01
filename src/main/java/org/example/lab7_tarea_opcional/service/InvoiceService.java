
package org.example.lab7_tarea_opcional.service;

import org.example.lab7_tarea_opcional.dto.InvoiceDetailForm;
import org.example.lab7_tarea_opcional.dto.InvoiceForm;
import org.example.lab7_tarea_opcional.entity.Customer;
import org.example.lab7_tarea_opcional.entity.Invoice;
import org.example.lab7_tarea_opcional.entity.InvoiceDetail;
import org.example.lab7_tarea_opcional.entity.Product;
import org.example.lab7_tarea_opcional.respository.CustomerRepository;
import org.example.lab7_tarea_opcional.respository.InvoiceDetailRepository;
import org.example.lab7_tarea_opcional.respository.InvoiceRepository;
import org.example.lab7_tarea_opcional.respository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceDetailRepository invoiceDetailRepository,
                          CustomerRepository customerRepository,
                          ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    //Validamos los datos del comprobante antes de guardarlo
    public void validar(InvoiceForm form, BindingResult result) {

        //Comprobamos que el cliente exista y que su documento corresponda al tipo de comprobante seleccionado
        if (form.getCustomerId() != null) {
            Customer customer = customerRepository.findById(form.getCustomerId()).orElse(null);

            if (customer == null) {
                result.rejectValue("customerId", "cliente.inexistente",
                        "El cliente seleccionado no existe");
            } else if ("Factura".equals(form.getType())
                    && !"RUC".equals(customer.getDocumentType())) {
                result.reject("tipo.incompatible",
                        "Una factura solo puede emitirse a un cliente con RUC");
            } else if ("Boleta".equals(form.getType())
                    && !"DNI".equals(customer.getDocumentType())) {
                result.reject("tipo.incompatible",
                        "Una boleta solo puede emitirse a un cliente con DNI");
            }
        }

        //Recorremos los productos seleccionados
        int seleccionados = 0;
        Set<Integer> usados = new HashSet<>();
        List<InvoiceDetailForm> detalles = form.getDetails();

        for (int i = 0; i < detalles.size(); i++) {
            InvoiceDetailForm d = detalles.get(i);

            if (!d.isSelected()) {
                continue;
            }
            seleccionados++;

            String campo = "details[" + i + "].quantity";

            Product producto = null;
            if (d.getProductId() != null) {
                producto = productRepository.findById(d.getProductId()).orElse(null);
            }

            if (producto == null) {
                result.reject("producto.inexistente",
                        "Hay un producto seleccionado que no existe");
                continue;
            }

            //Comprobamos que no se repita el mismo producto.
            if (!usados.add(producto.getId())) {
                result.reject("producto.repetido",
                        "El producto " + producto.getName() + " está repetido en el pedido");
                continue;
            }

            //Si la cantidad tiene un error de conversión, no seguimos validándola
            if (result.hasFieldErrors(campo)) {
                continue;
            }

            Integer cantidad = d.getQuantity();
            if (cantidad == null || cantidad < 1) {
                result.rejectValue(campo, "cantidad.invalida",
                        "Ingrese una cantidad entera mayor que cero");
            } else if (cantidad > producto.getStock()) {
                result.rejectValue(campo, "cantidad.stock",
                        "La cantidad supera el stock disponible (" + producto.getStock() + ")");
            }
        }

        //Comprobamos que se haya seleccionado al menos un producto
        if (seleccionados == 0) {
            result.reject("detalle.vacio",
                    "Debe incluir al menos un producto en el pedido");
        }
    }

    //Guardamos el comprobante y sus detalles
    @Transactional
    public Invoice guardarComprobante(InvoiceForm form) {

        Customer customer = customerRepository.findById(form.getCustomerId()).orElseThrow();

        Invoice invoice = new Invoice();
        invoice.setType(form.getType());
        invoice.setDate(form.getDate());
        invoice.setCustomer(customer);
        invoice = invoiceRepository.save(invoice);

        for (InvoiceDetailForm d : form.getDetails()) {
            if (!d.isSelected()) {
                continue;
            }

            Product producto = productRepository.findById(d.getProductId()).orElseThrow();

            InvoiceDetail detalle = new InvoiceDetail();
            detalle.setInvoice(invoice);
            detalle.setProduct(producto);
            detalle.setQuantity(d.getQuantity());
            detalle.setPrice(producto.getPrice());
            detalle.setSubtotal(calcularSubtotal(d.getQuantity(), producto.getPrice()));
            invoiceDetailRepository.save(detalle);

            //Actualizamos el stock después de registrar el producto
            producto.setStock(producto.getStock() - d.getQuantity());
            productRepository.save(producto);
        }

        return invoice;
    }

    //Calculamos el subtotal multiplicando la cantidad por el precio
    public Double calcularSubtotal(Integer cantidad, Double precio) {
        return Math.round(cantidad * precio * 100.0) / 100.0;
    }

    //Sumamos los subtotales para obtener el total del comprobante
    public Double calcularTotal(List<InvoiceDetail> detalles) {
        double total = 0;
        for (InvoiceDetail d : detalles) {
            total += d.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    //Obtenemos el total de cada comprobante para mostrarlo en la lista
    public Map<Integer, Double> calcularTotales(List<Invoice> comprobantes) {
        Map<Integer, Double> totales = new HashMap<>();
        for (Invoice c : comprobantes) {
            totales.put(c.getId(),
                    calcularTotal(invoiceDetailRepository.findByInvoiceId(c.getId())));
        }
        return totales;
    }
}
