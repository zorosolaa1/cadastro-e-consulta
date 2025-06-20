package com.rodrigopettenon.cadastro_e_consulta.services;

import com.rodrigopettenon.cadastro_e_consulta.dtos.OrderDto;
import com.rodrigopettenon.cadastro_e_consulta.dtos.OrderPageDto;
import com.rodrigopettenon.cadastro_e_consulta.exceptions.ClientErrorException;
import com.rodrigopettenon.cadastro_e_consulta.models.ClientModel;
import com.rodrigopettenon.cadastro_e_consulta.models.OrderModel;
import com.rodrigopettenon.cadastro_e_consulta.models.OrderStatus;
import com.rodrigopettenon.cadastro_e_consulta.repositories.ClientRepository;
import com.rodrigopettenon.cadastro_e_consulta.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.rodrigopettenon.cadastro_e_consulta.utils.StringsValidation.removeAllSpaces;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Transactional
    public OrderDto saveOrder(OrderDto orderDto) {
        validateClientId(orderDto.getClientId());
        OrderStatus validatedOrderStatus = validateOrderStatus(orderDto.getStatus());
        orderDto.setOrderDate(LocalDateTime.now());

        ClientModel client = clientRepository.findClientModelById(orderDto.getClientId());

        OrderModel orderModel = new OrderModel();
        orderModel.setClient(client);
        orderModel.setOrderDate(orderDto.getOrderDate());
        orderModel.setStatus(validatedOrderStatus);

        return orderRepository.saveOrder(orderModel);
    }

    private void validateClientId(Long clientId) {
        if (isNull(clientId)) {
            throw new ClientErrorException("O id do cliente é obrigatório.");
        }
        if (!clientRepository.existsClientById(clientId)) {
            throw new ClientErrorException("Não existe cliente cadastrado com o id informado: " + clientId);
        }
    }

    private OrderStatus validateOrderStatus(String status) {
        try{
            if (!isBlank(status)) {
                String sanitizedStatus = removeAllSpaces(status.toUpperCase());
                return OrderStatus.valueOf(sanitizedStatus);
            }
            else {
                throw new ClientErrorException("O status é obrigatório.");
            }
        } catch (IllegalArgumentException e) {
            throw new ClientErrorException("O status informado é inválido: " + status);
        }
    }

    public OrderDto findById(UUID id) {
        validateId(id);

        return orderRepository.findOrderById(id);
    }

    private void validateId(UUID id) {
        if (isNull(id)) {
            throw new ClientErrorException("O id do pedido é obrigatório.");
        }
        if (!orderRepository.existsOrderById(id)){
            throw new ClientErrorException("O id do pedido não está cadastrado.");
        }
    }

    public OrderPageDto findFilteredOrders(UUID id, Long clientId, LocalDateTime dateTimeStart,
                                           LocalDateTime dateTimeEnd, String status, Integer page,
                                           Integer linesPerPage, String direction, String orderBy) {

        return orderRepository.findFilteredOrders(id, clientId, dateTimeStart, dateTimeEnd, status, page, linesPerPage, direction, orderBy);
    }
}
