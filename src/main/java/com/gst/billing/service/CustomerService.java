package com.gst.billing.service;

import com.gst.billing.dto.CustomerDto;
import com.gst.billing.entity.Customer;
import com.gst.billing.exception.InvalidGstinException;
import com.gst.billing.exception.ResourceNotFoundException;
import com.gst.billing.repository.CustomerRepository;
import com.gst.billing.util.GstinValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for customer management with GSTIN validation.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final GstinValidator gstinValidator;

    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public CustomerDto getCustomerById(Long id) {
        return toDto(findOrThrow(id));
    }

    public CustomerDto createCustomer(CustomerDto dto) {
        // Validate GSTIN format if provided
        if (dto.getGstin() != null && !dto.getGstin().isBlank()) {
            if (!gstinValidator.isValid(dto.getGstin())) {
                throw new InvalidGstinException(dto.getGstin());
            }
            if (customerRepository.existsByGstin(dto.getGstin())) {
                throw new IllegalArgumentException("A customer with GSTIN " + dto.getGstin() + " already exists");
            }
        }

        Customer customer = Customer.builder()
            .name(dto.getName())
            .gstin(dto.getGstin())
            .state(dto.getState())
            .build();
        return toDto(customerRepository.save(customer));
    }

    public CustomerDto updateCustomer(Long id, CustomerDto dto) {
        Customer customer = findOrThrow(id);

        if (dto.getGstin() != null && !dto.getGstin().isBlank()) {
            if (!gstinValidator.isValid(dto.getGstin())) {
                throw new InvalidGstinException(dto.getGstin());
            }
        }

        customer.setName(dto.getName());
        customer.setGstin(dto.getGstin());
        customer.setState(dto.getState());
        return toDto(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        findOrThrow(id);
        customerRepository.deleteById(id);
    }

    // ---- Internal helpers ----

    public Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private CustomerDto toDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setGstin(c.getGstin());
        dto.setState(c.getState());
        return dto;
    }
}
