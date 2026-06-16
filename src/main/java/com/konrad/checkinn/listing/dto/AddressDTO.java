package com.konrad.checkinn.listing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "Please provide House No., Flat no etc.")
    private String houseNumber;
    @NotBlank(message = "Please provide Street Address")
    private String streetAddress;
    @NotBlank(message = "Please provide State")
    private String state;
    @NotBlank(message = "Please provide Country")
    private String country;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Please provide a valid pincode")
    private String pincode;

    private Double latitude;
    private Double longitude;
}
