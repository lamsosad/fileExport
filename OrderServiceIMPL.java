package d5.warehousemanagement.service.ServiceIMPL;

import d5.warehousemanagement.model.Orders;
import d5.warehousemanagement.repository.IOrderRepository;
import d5.warehousemanagement.service.IService.IOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceIMPL implements IOrderService {
    private final IOrderRepository orderRepository;

    @Autowired
    public OrderServiceIMPL(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Page<Orders> search(String orderCode, String phoneNumber, Integer status, String wareHouseCode, Pageable pageable) {
        orderCode = StringUtils.isBlank(orderCode) ? null : orderCode;
        phoneNumber = StringUtils.isBlank(phoneNumber) ? null : phoneNumber;
        wareHouseCode = StringUtils.isBlank(wareHouseCode) ? null : wareHouseCode;
        return orderRepository.search(orderCode,phoneNumber,status,wareHouseCode,pageable );
    }
}
