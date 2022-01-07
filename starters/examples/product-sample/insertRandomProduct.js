const axios = require('axios');

let randomSt = (Math.random() + 1).toString(36).substring(7);

for (let i = 0; i < 10; i++) {
    const data = []
    for (let j = 0; j < 50; j++) {
        const regular_price = randomIntFromInterval(10, 1000)
        data.push({
            id: `random-product-${randomSt}-${i}-${j}`,
            name: `Random Product ${randomSt} ${i} ${j}`,
            description: `A random product description for ${randomSt} ${i} ${j}`,
            available: true,
            status: "PUBLISH",
            regular_price: regular_price,
            sale_price: regular_price * randomIntFromInterval(50, 90) / 100,
        })

    }
    axios.post('http://localhost:8080/data/product', { data: data })
    .then(function (response) {
        // handle success
        console.log(response.status);
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })
}


function randomIntFromInterval(min, max) { // min and max included 
    return Math.floor(Math.random() * (max - min + 1) + min)
}