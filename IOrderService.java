package d5.warehousemanagement.service.IService;

import d5.warehousemanagement.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


public interface IOrderService {
    Page<Orders> search(
            @Param("orderCode") String orderCode,
            @Param("phoneNumber") String phoneNumber,
            @Param("status") Integer status,
            @Param("wareHouseCode") String wareHouseCode,
            Pageable pageable);
}
