//user控制层 
app.controller('userController' ,function($scope, userService){

	$scope.entity = {phone: ''};

	// 保存
	$scope.save = function() {
		userService.save($scope.entity, $scope.myCode).success(function(response) {
			if (response.success) {
				location.href = "login.html";
			} else {
				alert(response.message);
			}
		});
	}

	// 发送短信验证码
	$scope.sendCode = function() {

		if($scope.entity.phone == '') {
			alert("请输入手机号");
			return;
		}
		userService.sendCode($scope.entity.phone).success(function(resp){
			if(resp.success) {
				var i = 10;
				$("#myBtn").attr("disabled", "disabled");
				var timer = setInterval(function(){
					if(i <= 0) {
						$("#myBtn").removeAttr("disabled");
						$("#myBtn").html("获取验证码");
						clearInterval(timer);
					} else {
						$("#myBtn").html(--i + "秒后重发");
					}
				}, "1000");

			} else {
				alert(resp.message);
			}
		});
	}

});
