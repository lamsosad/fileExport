package d5.warehousemanagement.controller;

import d5.warehousemanagement.model.Orders;
import d5.warehousemanagement.service.IService.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/system")
@RestController
public class OrderController {
    @Autowired
    private IOrderService orderService;



    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required = false) String orderCode,
                                    @RequestParam(required = false) String phoneNumber,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) String wareHouseCode,
                                    @RequestParam(defaultValue = "0") int pageNumber,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(defaultValue = "orderCode,asc") String[] sort) {
        Sort sorting = Sort.by(sort[0]);
        if (sort[1].equals("desc")) {
            sorting = sorting.descending();
        } else {
            sorting = sorting.ascending();
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sorting);
        Page<Orders> ordersPage = orderService.search(orderCode,phoneNumber,status,wareHouseCode,pageable);
        return new ResponseEntity<>(ordersPage, HttpStatus.OK);
    }
}
