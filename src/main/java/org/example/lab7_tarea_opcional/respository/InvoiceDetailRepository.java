package org.example.lab7_tarea_opcional.respository;

import org.example.lab7_tarea_opcional.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {


    //Buscamos los detalles de una factura según su ID
    List<InvoiceDetail> findByInvoiceId(Integer invoiceId);
}
