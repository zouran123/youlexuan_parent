app.controller('itemController', function($scope, $http){

	// 购买数量
	$scope.num = 1;


	$scope.changNum = function(x) {
	
	
		$scope.num = $scope.num + x;
	
		if($scope.num <= 0 ) {
			$scope.num = 1;
		}
	
	}

	//记录用户选择的规格
	$scope.specificationItems = {};

	$scope.updateSpec = function(specName, opName) {
		$scope.specificationItems[specName] = opName;
		
		// 更改进入页面默认的sku变量
		changeSku();
		
	}
	
	// 判断某个规格选项是否选中
	$scope.isSelected = function(specName, opName) {
	
	
		if($scope.specificationItems[specName] != opName) {
		
			return false;
		} else {
			return true;
			
		}
		
	
	}
	
	
	
	// 进入页面，加载默认的sku
	$scope.loadDefaultSku = function() {
	
		// 变量sku是一个完整信息的sku，即如果点击购物车，是提交到后台的信息
		$scope.sku = itemList[0];
		
	
		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
	
	
	}
	
	
	// 更改进入页面默认的sku变量
	changeSku = function() {
	
		for(var i = 0; i < itemList.length; i++) {
		
			if(equalsObj(itemList[i].spec, $scope.specificationItems)) {
		
				$scope.sku = itemList[i];
				return;
		
			}
		}
	
	}
	
	
	// 判断两个对象是否相等
	equalsObj = function(obj1, obj2) {
	
		for(var k in obj1) {
			if(obj1[k] != obj2[k]) {
				return false;
			}
		}
	
		for(var k in obj2) {
		
			if(obj2[k] != obj1[k]) {
				return false;
			}
		
		}
		return true;
	}
	
	
	$scope.addCart = function() {
		$http.get('http://localhost:9013/cart/addGoodsToCartList.do?skuId=' + $scope.sku.id + '&num=' + $scope.num, {'withCredentials':true}).success(function(resp){
			if(resp.success) {
				location.href = 'http://localhost:9013/cart.html';
			} else {
				alert(resp.message);
			}
		});
		// alert("添加的商品ID是：" + $scope.sku.id + "，数量是：" + $scope.num);
	}

});
