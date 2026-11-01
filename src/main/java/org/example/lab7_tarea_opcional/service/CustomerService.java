package org.example.lab7_tarea_opcional.service;

import org.example.lab7_tarea_opcional.entity.Customer;
import org.example.lab7_tarea_opcional.respository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    //Comprobamos si un documento ya está registrado
    public boolean documentoDuplicado(String document, Integer id) {

        //Si el ID es null, significa que estamos registrando un cliente nuevo
        if (id == null) {

            //Evaluamos si algún cliente ya tiene ese documento
            return customerRepository.existsByDocument(document);
        }

        //Comprobamos si otro cliente tiene el mismo documento, excluyendo al cliente cuyo ID se ha recibido
        return customerRepository.existsByDocumentAndIdNot(document, id);
    }



    //Evaluamos si el documento tiene el formato correcto según el tipo de documento seleccionado
    public boolean documentoValidoParaTipo(String document, String documentType) {

        //Si falta el documento o el tipo, la validación falla
        if (document == null || documentType == null) {
            return false;
        }

        //Si el tipo es DNI, comprobamos que tenga exactamente 8 dígitos del 0 al 9
        if ("DNI".equals(documentType)) {
            return document.matches("[0-9]{8}");
        }
        //Si el tipo es RUC, comprobamos que tenga exactamente 11 dígitos del 0 al 9
        if ("RUC".equals(documentType)) {
            return document.matches("[0-9]{11}");
        }

        //Si el tipo no es DNI ni RUC, devolvemos false
        return false;
    }

    public Customer guardar(Customer customer) {
        return customerRepository.save(customer);
    }


}
