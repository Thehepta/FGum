
var offset = 0xc2140

function main(){
    Java.perform(function(){
        let NativeLibHelper = Java.use("com.adjust.sdk.sig.NativeLibHelper");
        NativeLibHelper["nSign"].implementation = function (context, obj, bArr, i) {
            hook()
            hook_adbytes()
            let ret = this.nSign(context, obj, bArr, i);
            console.log('nSign ret value is ' + ret);
            return ret;
        };
    })
}
function hook(){
    let signer = Module.findBaseAddress("libsigner.so");
    Interceptor.attach(signer.add(offset),{
        onEnter:function(args){
            console.log("init ->")
            console.log(hexdump(args[1],{length:0x20,header:false,ansi:true}))
        },
        onLeave:function(){}
    })
}

function hook_adbytes(){
    Java.perform(function () {

        let signer = Module.findBaseAddress("libsigner.so");
        console.log(signer)
        Interceptor.attach(signer.add(offset+0x4c),{
            onEnter:function(args){
                console.log("update ->")
                console.log(hexdump(args[1],{length:0x40,header:false,ansi:true}))
            },onLeave:function(){

            }
        })
    });

}


setImmediate(main)
