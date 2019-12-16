//seckill_goods控制层 
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService, seckillOrderService) {
    //查询列表
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    // 根据id查询单个秒杀商品
    $scope.findOne = function () {
        seckillGoodsService.findOne($location.search()['sekKillId']).success(function (resp) {
            if (resp) {
                $scope.entity = resp;
                // Math.random   Math.round   Math.ceil   Math.floor
                var allSeconds = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);

                timer = $interval(function () {
                    if (allSeconds > 0) {
                        allSeconds -= 1;
                        $(".overtime").html(convertTimeString(allSeconds));
                    } else {
                        $interval.cancel(timer);
                    }
                }, 1000);

            }
        });
    }


    //转换秒为天小时分钟秒，格式：XXX天 10:22:33
    convertTimeString = function (allsecond) {
        var days = Math.floor(allsecond / (60 * 60 * 24));//天数

        var hours = Math.floor((allsecond - days * 60 * 60 * 24) / (60 * 60));//小时数
        hours = hours < 10 ? "0" + hours : hours;

        var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟数
        minutes = minutes < 10 ? "0" + minutes : minutes;

        var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60; //秒数
        seconds = seconds < 10 ? "0" + seconds : seconds;

        var timeString = "";
        if (days > 0) {
            timeString = days + "天 ";
        }
        return "距离结束：" + timeString + hours + ":" + minutes + ":" + seconds;
    }


    // 提交订单
    $scope.submitOrder = function(id) {
        seckillOrderService.submitOrder(id).success(function(resp){
            if(resp.success) {
                location.href = "pay.html";
            } else {
                alert(resp.message);
                location.href = "seckill-index.html";
            }
        });
    }

});	
