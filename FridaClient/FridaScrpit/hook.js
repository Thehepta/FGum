function main() {
    Java.perform(function () {
         var i = 0;
         var timer = setInterval(function () {
             console.log(i++);
         },1000)
    })
}

setImmediate(main);