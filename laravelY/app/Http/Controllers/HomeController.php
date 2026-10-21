<?php

namespace App\Http\Controllers;

use http\Env\Request;
use Illuminate\Support\Facades\DB;

class HomeController
{
    public  function cate($id){
        return DB::table('category')->where(['parent_id'=>$id,'delete_flag'=>0])->get();
    }

    public function hot(){
        $yz= DB::table('product')->where(['hot_flag'=>'1'])->get();
        return [
            "code"=>200,
            "status"=>20000,
            "data"=>$yz
        ];
    }
    public function phone(){
        $yz= DB::table('product')->where(['delete_flag'=>'0'])->get();
        return [
            "code"=>200,
            "status"=>20000,
            "data"=>$yz
        ];
    }

    public  function  details($id){
//        return $id;
        $yz= DB::table('product')->where(['id'=>$id])->get();
        return [
            "code"=>200,
            "status"=>20000,
            "data"=>$yz
        ];
    }
    public  function  page(\Illuminate\Http\Request $request){
        $data=$request->all();
        $yz= DB::table('product')->where(['delete_flag'=>'0'])->paginate($data['pageSize'])->toArray();
        return  $yz;
        return [
            "code"=>200,
            "status"=>20000,
            "data"=>$yz
        ];
    }


    public  function  proUpdate(\Illuminate\Http\Request $request){
//        return $request;
        $data=$request->all();
        return DB::table('product')->where(['id'=>$data['id']])->update($data);
    }


    public function proDelete($id){
        return DB::table('product')->where(['id'=>$id])->update(['delete_flag'=>1]);
    }

}
