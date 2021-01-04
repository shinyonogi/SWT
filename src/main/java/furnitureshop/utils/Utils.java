package furnitureshop.utils;

import furnitureshop.inventory.ItemCatalog;
import furnitureshop.lkw.LKWCatalog;
import furnitureshop.order.OrderService;
import furnitureshop.order.ShopOrder;
import furnitureshop.supplier.SupplierRepository;
import org.salespointframework.order.OrderManagement;
import org.springframework.stereotype.Service;

@Service
public class Utils {

	private static OrderService orderService;

	private static OrderManagement<ShopOrder> orderManagement;
	private static LKWCatalog lkwCatalog;
	private static SupplierRepository supplierRepository;
	private static ItemCatalog itemCatalog;

	private Utils(OrderService orderService, OrderManagement<ShopOrder> orderManagement, LKWCatalog lkwCatalog,
			SupplierRepository supplierRepository, ItemCatalog itemCatalog) {
		Utils.orderService = orderService;
		Utils.orderManagement = orderManagement;
		Utils.lkwCatalog = lkwCatalog;
		Utils.supplierRepository = supplierRepository;
		Utils.itemCatalog = itemCatalog;
	}

	public static void clearRepositories() {
		for (ShopOrder order : orderService.findAll()) {
			orderManagement.delete(order);
		}

		lkwCatalog.deleteAll();
		itemCatalog.deleteAll();
		supplierRepository.deleteAll();
	}

}
