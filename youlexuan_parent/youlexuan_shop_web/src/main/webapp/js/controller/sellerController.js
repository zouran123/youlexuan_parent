//seller控制层 
app.controller('sellerController' ,function($scope, sellerService){
	// 保存
	$scope.save = function() {
		sellerService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				location.href = "/shoplogin.html";
			} else {
				alert(response.message);
			}
		});
	}
});
