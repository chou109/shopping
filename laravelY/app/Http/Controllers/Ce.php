<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Session;
use Illuminate\Support\Str;

class Ce
{
public function ce(){
    $token=  Str::random(32);
    Session::put($token,1);
    return Session::all();
}
public  function ca(){
   return Session::all();

}
}
