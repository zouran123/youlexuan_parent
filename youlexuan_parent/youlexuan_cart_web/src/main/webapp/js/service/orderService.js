//order服务层
app.service('orderService', function($http){

	this.submitOrder = function(order) {
		return $http.post('order/add.do', order);
	}

});