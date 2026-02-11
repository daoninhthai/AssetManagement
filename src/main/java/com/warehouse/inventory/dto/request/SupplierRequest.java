package com.warehouse.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    private String name;

    private String contactPerson;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;

    private String address;
}
