package org.example.lab7_tarea_opcional.dto;

// Una fila del formulario es un producto del catálogo
public class InvoiceDetailForm {

    private Integer productId;   //Guardamos el ID del producto
    private Integer quantity;    //Indicamos cuántas unidades se van a pedir
    private boolean selected;    //Indicamos si el producto fue seleccionado

    public InvoiceDetailForm() {
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}