//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService,
                                            uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()["id"] //获取地址栏中的id参数值
        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = {};

                $scope.entity = response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //SKU列表规格列转换
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec =
                        JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    //保存
    $scope.save = function () {
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert('保存成功');
                    $scope.entity = {};
                    editor.html("");
                    location.href="goods.html";//跳转到商品列表页
                } else {
                    alert(response.message);
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
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //保存
    $scope.add = function () {
        //提取富文本的内容
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert('保存成功');
                    $scope.entity = {};
                    //清空富文本编辑器
                    editor.html('');
                } else {
                    alert(response.message);
                }
            }
        );

        /* var serviceObject;//服务层对象
         $scope.entity.goodsDesc.introduction=editor.html();

         serviceObject = goodsService.add( $scope.entity  );//增加
         serviceObject.success(
             function(response){
                 if(response.success){
                     //重新查询
                     $scope.entity={};//重新加载
                     editor.html('');//清空
                 }else{
                     alert(response.message);
                 }
             }
         );*/

    }

    /**
     * 上传图片
     */

    //初始化对象
    $scope.entity = {goods: {}, goodsDesc: {itemImages: []}};

    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {//如果上传成功，取出url
                $scope.image_entity.url = response.message;//设置文件地址

            } else {
                alert(response.message);
            }
        }).error(function () {
            alert("上传发生错误");
        });
    };

    //点保存照片就加入到集合
    $scope.add_image_entity = function () {
        //加入照片集合
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //加载一级分类目录
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )
    }

    //监听一级目录的更改 $watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数。
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        // 清空下级目录
        $scope.itemCat3List = {};
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    })

    //监听二级目录的更改
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    })

    //监听三级目录的更改
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {

        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;


            }
        )
    })

    //监听模板ID的改变
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;
                //品牌
                $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
                //扩展属性
                if ($location.search()['id'] == null) {

                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
                }

            }
        )
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                // alert(JSON.stringify(response))
                $scope.specList = response;
            }
        )
    })

    /**
     * 如果有就返回对象, 没有就返回null
     * @param list 集合
     * [{"attributeName":xxxx,"attributeValue":[]}]
     * @param key key
     * @param keyValue value
     */
    //从集合中按照key查询对象
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }

    $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};

    $scope.updateSpecAttribute = function ($event, name, value) {
        //[{"attributeName":xxxx,"attributeValue":[]}]
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (obj != null) {
            if ($event.target.checked) {
                //选中勾选,加入集合
                obj.attributeValue.push(value);
            } else {
                //取消勾选,移出集合
                obj.attributeValue.splice(obj.attributeValue.indexOf(value), 1);
                //如果选项都取消了，将此条记录移除
                if (obj.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(obj), 1);
                }
            }
        } else {
            //之前不存在
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }

    //构件sku列表
    $scope.createItemList = function () {
        /**
         * 初始化一个sku列表
         * spec:{"网络":"3G,"内存":2G}
         * spec:{"网络":"3G,"内存":4G}
         */
        $scope.entity.itemList = [{spec: {}, price: 0, num: 9999, statu: 0, isDefault: 0}];

        //{{entity.goodsDesc.specificationItems}}
        var items = $scope.entity.goodsDesc.specificationItems;

        for (var i = 0; i < items.length; i++) {
            //深克隆,$scope.entity.itemList已经不是原来那个对象了
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);


        }
    }
    //不写$scope,代表此controller内部访问, 相当于私有化方法
    /**
     *
     * @param list : [
     *          {spec:{"网络":"3G,"内存":2G},price:0,num:9999,statu:0,isDefault:0},
     *          {spec:{"网络":"3G,"内存":4G},price:0,num:9999,statu:0,isDefault:0}
     *      ]
     * @param columnName : 网络
     * @param conlumnValues : ["移动4G","联通3G"]
     */
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];

            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }


    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//商品状态

    $scope.itemCatList = [];//商品分类列表
    //加载商品分类列表
    //代码解释：因为我们需要根据分类ID得到分类名称，所以我们将返回的分页结果以数组形式再次封装。
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    //将每个商品分类的id放入集合,value是分类名
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        )
    }


    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue = function (specName, optionName) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }

});
