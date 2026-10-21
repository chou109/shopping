<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class RechargeController
{
    public  function rechargeInfo(Request  $request){
//        return $request;
        $data=$request->all();
        return   DB::table('recharge_info')->insert($data);

    }
    public  function balance( $id,Request $request){
        return DB::table('user')->where(['id'=>$id])->update(['balance'=>$request['yu']]);

    }

    public function rechargePage(Request  $request){
//        return $request;
        $data = DB::table('recharge_info')->get()->toArray();
        rsort($data);
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

    public function rechargeUser(){
     return 123;
    }

}
