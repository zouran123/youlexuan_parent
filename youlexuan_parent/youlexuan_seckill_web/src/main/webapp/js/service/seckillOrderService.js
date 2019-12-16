//seckill_order服务层
app.service('seckillOrderService', function($http){
	// 提交订单
	this.submitOrder = function(id) {
		return $http.get('seckillOrder/submitOrder.do?id=' + id);
	}

});