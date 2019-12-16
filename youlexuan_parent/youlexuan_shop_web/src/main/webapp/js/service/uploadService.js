//文件上传服务层
app.service('uploadService', function($http){

	this.uploadFile = function () {
		var data = new FormData();
		// 将文件对象挂到表单对象中
		data.append("file", file.files[0]);

		// 上传文件，需要设置更多的属性
		return $http({
			method : 'POST',
			url : "../upload.do",
			data : data,
			// 针对angular的配置
			headers : {
				'Content-Type' : undefined
			},
			transformRequest : angular.identity
		});

	}

});