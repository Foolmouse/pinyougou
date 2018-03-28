 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
		    if($scope.grade==1){
		        $scope.entity.parentId = 0;
            }
            if($scope.grade==2){
		        $scope.entity.parentId = $scope.entity_1.id;
            }
            if($scope.grade==3){
		        $scope.entity.parentId = $scope.entity_2.id;
            }
			serviceObject=itemCatService.add( $scope.entity  );//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    //根据上级ID显示下级列表
    $scope.findByParentId=function(parentId){
        itemCatService.findByParentId(parentId).success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //面包屑导航
    $scope.grade=1;//默认为1级
    //设置级别
    $scope.setGrade=function(value){
        $scope.grade=value;
    }

    //读取列表
    /*
    进入页面初始化grade为1
    点击下一级 , grade+1 , 查询种类下的商品 , 同时将自身作为第二级目录显示在面包屑
    点击第二级面包屑目录, 重置grade , 并查询目录下商品
    so easy.
     */
    $scope.selectList=function(p_entity){
        if($scope.grade==1){//如果为1级
            $scope.entity_1=null;
            $scope.entity_2=null;
        }
        if($scope.grade==2){//如果为2级
            $scope.entity_1=p_entity;
            $scope.entity_2=null;
        }
        if($scope.grade==3){//如果为3级
            $scope.entity_2=p_entity;
        }
        $scope.findByParentId(p_entity.id);	//查询此级下级列表
    }
});	
