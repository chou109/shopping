<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CartController
{
    public  function cart($id,Request $request){
        $yz=DB::table('cart')->where(['delete_flag'=>0,'user_id'=>$id])->get();
//        $token=$request->session()->get('token');
        return $yz;
    }

    public  function  modify(Request $request){
        $date=$request->all();
//        return $date;
        return DB::table('cart')->where(['id'=>$request['id']])->update($date);
//        return $request->all();
    }

    public function  del($id){
        return DB::table('cart')->delete($id);
    }

    public function cartAdd(Request  $request){
        $data=$request->all();
//        return  $data;
        return DB::table('cart')->insert($data);
    }

}
