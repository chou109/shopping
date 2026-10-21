<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class OrderController
{
    public function order($id, Request $request)
    {
        $data = DB::table('order_detail')->where(['delete_flag' => '0', 'create_user' => $id])->get()->toArray();
        rsort($data);
//        return count($data);
        $page = $request['page'];
        $perPage = $request['pageSize'];
        $offset = ($page - 1) * $perPage;
        $da = array_slice($data, $offset, $perPage);
        return [
            "code" => 200,
            "status" => 20000,
            "data" => $da,
            'total' => count($data)

        ];
    }

    public function detail($id, Request $request)
    {
//        return $request;
//
        $rand = rand(100, 999);
        $data = $request->all();
        $data['status_desc'] = "未付款";
        $data['create_user'] = $id;
        $data['status'] = 10;
        $data['order_detail_no'] = (string)$request['order_no'] . (string)$rand;
//return  $data;
        return DB::table('order_detail')->insert($data);

    }


    public function orders(Request $request)
    {
        $data=$request->all();
//        return  $data['order_on'];
        return DB::table('order_detail')->where(['order_no' => $data['order_on']])->get();
    }

    public  function orderShopping(Request  $request){

        $data=$request->all();
//        return $data;
        return DB::table('order_detail')->where(['order_no' => $data['order_no']])->update(['status'=>"20",'status_desc'=>"已付款"]);

    }


}
