<?php

namespace App\Http\Controllers;

use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Foundation\Bus\DispatchesJobs;
use Illuminate\Foundation\Validation\ValidatesRequests;
use Illuminate\Routing\Controller as BaseController;
use Illuminate\Support\Facades\DB;

class PostController extends BaseController
{
    use AuthorizesRequests, DispatchesJobs, ValidatesRequests;

    public  function  index(){
//        return "getsss";
        $db=DB::select('select * from user');
        return $db;
//        return view('text',['id'=>5]);
    }
    public  function save(){
        return "save";
    }
    public  function delete(){
        return "delete";
    }
    public  function update(){
        return "update";
    }
}
