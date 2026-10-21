<?php

use App\Models\User;
use Firebase\JWT\JWT;
use Illuminate\Support\Facades\Route;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;


Route::post('tokens/create', function() {
    // 创建用户

});

//登录
Route::post('/login',['App\Http\Controllers\LoginController','admin']);

//轮播图上层文字
Route::get('/gate/{id}',['App\Http\Controllers\HomeController','cate']);
Route::get('/product/hot',['App\Http\Controllers\HomeController','hot']);
Route::get('/product/phone',['App\Http\Controllers\HomeController','phone']);
Route::get('/product/{id}',['App\Http\Controllers\HomeController','details']);
Route::delete('/product/{id}',['App\Http\Controllers\HomeController','proDelete']);
Route::post('/product',['App\Http\Controllers\HomeController','proUpdate']);
//Route::get('/products/page',['App\Http\Controllers\HomeController','productPage']);
Route::get('/page/product/whole',['App\Http\Controllers\HomeController','page']);


Route::get('/cart/{id}',['App\Http\Controllers\CartController','cart']);
Route::post('/cart',['App\Http\Controllers\CartController','modify']);
Route::delete('/cart/{id}',['App\Http\Controllers\CartController','del']);
Route::post('/cart/add',['App\Http\Controllers\CartController','cartAdd']);


Route::get('/address/{id}',['App\Http\Controllers\Addcontroller','add']);
Route::delete('/address/{id}',['App\Http\Controllers\Addcontroller','del']);
Route::post('/address/{id}',['App\Http\Controllers\Addcontroller','address']);


Route::get('/order/{id}',['App\Http\Controllers\OrderController','order']);
Route::post('/order/child/sss',['App\Http\Controllers\OrderController','orders']);
Route::post('/order/detail/{id}',['App\Http\Controllers\OrderController','detail']);
Route::post('/order',['App\Http\Controllers\OrderController','orderShopping']);

Route::get('/user/{id}',['App\Http\Controllers\UserController','user']);
Route::get('/user',['App\Http\Controllers\UserController','userGet']);
Route::get('/users/page',['App\Http\Controllers\UserController','userPage']);
Route::post('/users/re/{id}',['App\Http\Controllers\UserController','banRe']);
Route::delete('/user/{id}',['App\Http\Controllers\UserController','userDelete']);
Route::post('/user',['App\Http\Controllers\UserController','userUpdate']);
//Route::post('/shopping',['App\Http\Controllers\UserController','modifyUser']);

Route::post('/sss/balance/{id}',['App\Http\Controllers\RechargeController','balance']);

Route::post('/recharge/info',['App\Http\Controllers\RechargeController','rechargeInfo']);
Route::get('/recharge',['App\Http\Controllers\RechargeController','rechargePage']);




Route::get('/ce',['App\Http\Controllers\Ce','ce']);
Route::get('/ca',['App\Http\Controllers\Ce','ca']);

//Route::get('user/add',['App\Http\Controllers\UserController','add']);
//Route::post('user/store',['App\Http\Controllers\UserController','store']);
//Route::post('user/ssss',['App\Http\Controllers\UserController','ssss']);
//
////获取数据
//Route::get('/index',['\App\Http\Controllers\PostController','index']);
////新增数据
//Route::post('/save',['\App\Http\Controllers\PostController','save']);
////删除数据
//Route::delete('/delete',['\App\Http\Controllers\PostController','delete']);
////修改数据
//Route::put('/update',['\App\Http\Controllers\PostController','update']);
//
////use Illuminate\Http\Request;
//Route::match(['get','post'],'/text/{id?}', function (Illuminate\Http\Request $request,$id=666) {
//    $data=$request->all();
//    return $data;
////    return  view('text',['id'=> $id]);
//});
//
//Route::group(["prefix"=>"admin","middleware"=>"AdminApiAuth"],function (){
//    Route::get('/user/{id}',function ($id){
//        return $id;
//    });
//    Route::post('/user/{id}',function (Request $request,$id=1025){
//        $validator = Validator::make($request->all(), [
//            'title' => 'required|max:2|isPositiveInteger',
//            'body' => 'required',
//        ],[
//            'title.required'=>'标题必传',
//            'body.required'=>'body必传',
//            'title.max'=>'最大长度不能超过2',
//            'title.is_positive_integer'=>'标题必须为正整数',
//        ]);
//        if ($validator->fails()) {
//            $result=[
//                "code"=>400,
//                "status"=>40000,
//                "message"=>array_values($validator->errors()->messages())[0][0],
//            ];
//            return response()->json($result,$result['code']);
//        }
//        return  $id;
//        return 666;
//    });
//});

//Route::prefix('admin')->group(function (){
//    Route::get('/user/{id}',function ($id){
//        return $id;
//    });
//});
