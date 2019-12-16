//login控制层
app.controller('loginController' ,function($scope, loginService){

	$scope.getName = function() {
		loginService.getName().success(function (resp) {
			$scope.loginName = resp.replace(/\"/g, "");
		})
	}


});
