<?php

namespace App\Providers;

use Illuminate\Foundation\Support\Providers\RouteServiceProvider as ServiceProvider;
use Illuminate\Support\Facades\Route;
use Validator;
class RouteServiceProvider extends ServiceProvider
{
    /**
     * This namespace is applied to your controller routes.
     *
     * In addition, it is set as the URL generator's root namespace.
     *
     * @var string
     */
    protected $namespace = 'App\Http\Controllers';

    /**
     * The path to the "home" route for your application.
     *
     * @var string
     */
    public const HOME = '/home';

    /**
     * Define your route model bindings, pattern filters, etc.
     *
     * @return void
     */
    public function boot()
    {
        parent::boot();
        //验证正整数
        Validator::extend('isPositiveInteger', function($attribute, $value, $parameters, $validator) {
            if (is_numeric($value) && is_int($value + 0) && ($value + 0) > 0) {
                return true;
            }else{
                return false;
            }
        });
        //验证0和1的状态
        Validator::extend('is_status_integer', function($attribute, $value, $parameters, $validator) {
            if (is_numeric($value) && is_int($value + 0) && in_array(($value + 0),[0,1])) {
                return true;
            }else{
                return false;
            }
        });
        //验证是否为数组并且里面的值必须是正整数
        Validator::extend('is_array_integer', function($attribute, $value, $parameters, $validator) {
            if(is_array($value) && count($value) > 0){
                foreach($value as $v){
                    if (!is_numeric($v) || !is_int($v + 0) || ($v + 0) <= 0) {
                        return false;
                    }
                }
            }else{
                return false;
            }
            return true;

        });
        //验证pid整数
        Validator::extend('is_pid_integer', function($attribute, $value, $parameters, $validator) {
            if (is_numeric($value) && is_int($value + 0) && ($value + 0) >= 0) {
                return true;
            }else{
                return false;
            }
        });
    }

    /**
     * Define the routes for the application.
     *
     * @return void
     */
    public function map()
    {
        $this->mapApiRoutes();

        $this->mapWebRoutes();

        //
    }

    /**
     * Define the "web" routes for the application.
     *
     * These routes all receive session state, CSRF protection, etc.
     *
     * @return void
     */
    protected function mapWebRoutes()
    {
        Route::middleware('web')
            ->namespace($this->namespace)
            ->group(base_path('routes/web.php'));
    }

    /**
     * Define the "api" routes for the application.
     *
     * These routes are typically stateless.
     *
     * @return void
     */
    protected function mapApiRoutes()
    {
        Route::prefix('api')
            ->middleware('api')
            ->namespace($this->namespace)
            ->group(base_path('routes/api.php'));
    }
}
