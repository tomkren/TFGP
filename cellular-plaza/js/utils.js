function log(x) {
  console.log(x);
}

function logg(x) {
  return function() {
    console.log(x);
  }
}

function sum(xs) {
  return _.reduce(xs, function(x,y) {
    return x+y;
  }, 0);
}

function showJson(json) {
  return JSON.stringify(json,null,2);
}

function fo(price) {
  //return Math.round(price);
  return price.toFixed(2);
}

function supports_html5_storage() {
  try {
    return 'localStorage' in window && window['localStorage'] !== null;
  } catch (e) {
    return false;
  }
}

function containsSubstring(str, substr) {
  str.indexOf(substr) !== -1;
}

function repeatStr(str, n) {
  return (n <= 0 ? '' : str+repeatStr(str,n-1));
}

function nop() {}
