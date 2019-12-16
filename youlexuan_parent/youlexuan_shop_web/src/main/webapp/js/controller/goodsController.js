//goods控制层 
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {
    // 继承
    $controller("baseController", {
        $scope: $scope
    });

    // 保存
    $scope.save = function () {
        // 将富文本编辑器中的内容赋值给entity
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.save($scope.entity).success(function (response) {
            if (response.success) {
                alert('操作成功');
                $scope.entity = {goods: {isEnableSpec:"0"}, goodsDesc: {itemImages: [], specificationItems:[]}};
                // 清空富文本编辑器的内容
                editor.html('');
            } else {
                alert(response.message);
            }
        });
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        //console.log("传递过来的id是：" + id);
        if(id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;

                // 富文本编辑器
                editor.html($scope.entity.goodsDesc.introduction);

                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                // 转换规格：itemList
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        );
    }

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();
                    $scope.selectIds = [];
                }
            }
        );
    }

    // 定义搜索对象
    $scope.searchEntity = {};
    // 搜索
    $scope.search = function (page, size) {
        goodsService.search(page, size, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        );
    }

    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (resp) {
            if (resp.success) {
                $scope.img_temp.url = resp.message;
            } else {
                alert(resp.message);
            }
        });
    }

    $scope.entity = {goods: {isEnableSpec:"0"}, goodsDesc: {itemImages: [], specificationItems:[]}};//定义页面实体结构
    // 保存图片到图片列表
    $scope.saveImg = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.img_temp);
    }

    // 移除图片列表
    $scope.delImg = function (idx) {
        $scope.entity.goodsDesc.itemImages.splice(idx, 1);
    }

    // 进入获取顶级分类
    $scope.selectItemCat1List = function (pId) {
        itemCatService.findParentById(pId).success(function (resp) {
            if (resp) {
                $scope.itemCat1List = resp;
            }
        });
    }

    // 监测一级分类的变化
    $scope.$watch('entity.goods.category1Id', function (newVal, oldVal) {
        if (newVal) {
            itemCatService.findParentById(newVal).success(function (resp) {
                if (resp) {
                    $scope.itemCat2List = resp;

                    // 清空3级分类的列表
                    $scope.itemCat3List = [];
                    $scope.entity.goods.typeTemplateId = null;
                }
            });
        }
    })

    // 监测二级分类的变化
    $scope.$watch('entity.goods.category2Id', function (newVal, oldVal) {
        if (newVal) {
            itemCatService.findParentById(newVal).success(function (resp) {
                if (resp) {
                    $scope.itemCat3List = resp;
                    $scope.entity.goods.typeTemplateId = null;
                }
            });
        }
    })

    // 监测三级分类的变化，获得模板id
    $scope.$watch('entity.goods.category3Id', function (newVal, oldVal) {
        if (newVal) {
            itemCatService.findOne(newVal).success(function (resp) {
                if (resp) {
                    $scope.entity.goods.typeTemplateId = resp.typeId;
                }
            });
        }
    })

    // 监测模板id的变化
    $scope.$watch('entity.goods.typeTemplateId', function (newVal, oldVal) {
        if (newVal) {
            typeTemplateService.findOne(newVal).success(function (resp) {
                if (resp) {
                    $scope.typeTemplate = resp;//获取类型模板
                    //将字符串的值，转为json格式
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表

                    // 当传递过来的id为空时，表示此次是新增商品，执行下面的语句
                    if($location.search()['id'] == null) {
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                }
            });
            // 根据模板id获取规格，以及规格对应的选项
            typeTemplateService.findSpecList(newVal).success(function (resp) {
                $scope.specList = resp;
            });

        }
    })

    // 更新选中的规格选项
    $scope.updateSpecOption = function ($event, specName, optionName) {
        var obj = $scope.findObjByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', specName);
        if(obj != null) {

            if($event.target.checked) {
                obj.attributeValue.push(optionName);
            } else {
                obj.attributeValue.splice(obj.attributeValue.indexOf(optionName), 1);

                if(obj.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj),1);
                }
            }

        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":specName,"attributeValue":[optionName]});
        }
    }

    // 根据选中的规格选项，动态创建item行
    $scope.creatItem = function() {
        $scope.entity.itemList = [{spec:{},price:0,num:999,status:'1',isDefault:'0'}];
        for (var i = 0; i < $scope.entity.goodsDesc.specificationItems.length; i++) {
            $scope.entity.itemList = addRow($scope.entity.itemList, $scope.entity.goodsDesc.specificationItems[i].attributeName, $scope.entity.goodsDesc.specificationItems[i].attributeValue);
        }
    }

    // 前面可以不加$scope,表示没有添加到$scope作用域中，只能在js中调，不能在页面调
    addRow = function (list, columnName, columnVal) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnVal.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[columnName] = columnVal[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
    /***************以上代码是goods_edit.html页面使用*****************/


    /***************以下代码是goods.html页面使用*****************/
    $scope.auditStatusName = ['未审核', '审核通过', '审核未通过', '关闭'];

    $scope.itemCatName = [];
    $scope.findItemCat = function() {
        itemCatService.findAll().success(function (resp) {
            for (var i = 0; i < resp.length; i++) {
                $scope.itemCatName[resp[i].id] = resp[i].name
            }
        });
    }

    $scope.checkOption = function (specName, optionName) {
        var obj = $scope.findObjByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', specName);
        if(obj == null) {
            return false;
        } else {
           if(obj.attributeValue.indexOf(optionName) >= 0) {
               return true;
           } else {
               return false;
           }
        }
    }

    // 提交重审
    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds, status).success(function (resp) {
            if(resp.success) {
                $scope.reloadList();
                $scope.selectIds = [];
            } else {
                alert(resp.messge);
            }
        })
    }


});	
