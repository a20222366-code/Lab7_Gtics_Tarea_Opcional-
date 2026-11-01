package org.example.lab7_tarea_opcional.respository;


import org.example.lab7_tarea_opcional.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
}
