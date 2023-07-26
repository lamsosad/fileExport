$(document).ready(function() {
    searchOrders();
});

function getCookie(name) {
    const cookieValue = document.cookie
        .split('; ')
        .find((cookie) => cookie.startsWith(name + '='));

    if (cookieValue) {
        return cookieValue.split('=')[1];
    }

    return null;
}

function getStatusString(status) {
    switch (status) {
        case 0:
            return "Đơn hàng mới";
        case 1:
            return "Lưu kho";
        case 2:
            return "Giao hàng thành công";
        case 3:
            return "Giao hàng thất bại";
        case 4:
            return "Hoàn hàng";
        default:
            return "Trạng thái không hợp lệ";
    }
}

function searchOrders() {
    var phoneNumber = document.getElementById("phoneNumber").value;
    var orderCode = document.getElementById("orderCode").value;
    var status = document.getElementById("status").value;
    var wareHouseCode = document.getElementById("wareHouseCode").value;
    var pageNumber = document.getElementById("pageNumber").value;
    var pageSize = document.getElementById("pageSize").value;
    var sort = document.getElementById("sort").value;

    const UrlApi = "/api/system/search?";
    fetch(UrlApi + new URLSearchParams({
        phoneNumber: phoneNumber,
        orderCode: orderCode,
        status: status,
        wareHouseCode: wareHouseCode,
        pageNumber: pageNumber,
        pageSize: pageSize,
        sort: sort,
    }), {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization":`Bearer ${getCookie("access_token")}`
        },
    }).then(response => {
        if (!response.ok) {
            throw new Error(`Error! status: ${response.status}`);
        }

        return response.json();
    })
        .catch(error => {
            console.log('error: ', error);
        })


        .then(data => {
            var results = document.getElementById("results");
            results.innerHTML = "";
            data.content.forEach(order => {
                var row = document.createElement("tr");
                var statusString = getStatusString(order.status);
                row.innerHTML =
                    "<td>" + order.id + "</td>" +
                    "<td>" + order.orderCode + "</td>" +
                    "<td>" + order.createdTime + "</td>" +
                    "<td>" + statusString + "</td>" +
                    "<td>" + order.wareHouse.wareHouseCode + "</td>" +
                    "<td>" + order.supplier.name + "</td>" +
                    "<td>" + order.supplier.phoneNumber + "</td>" +
                    "<td>" + order.recipientName + "</td>" +
                    "<td>" + order.recipientPhone + "</td>" +
                    "<td>" +
                    '<a href="/orderManager/OrderDetail.html">' +
                    '<button class="btn_add">Chi tiết</button>' +
                    '</a>' +
                    "</td>" +
                    "<td>" +
                    '<input type="checkbox" name="" id="" value=""/>' +
                    "</td>";

                results.appendChild(row);
            });

            var previousPageButton = document.getElementById("previousPage");
            var nextPageButton = document.getElementById("nextPage");
            var currentPageSpan = document.getElementById("currentPage");

            previousPageButton.disabled = data.first;
            nextPageButton.disabled = data.last;

            previousPageButton.onclick = function() {
                searchOrders(data.number - 1);
            };

            nextPageButton.onclick = function() {
                searchOrders(data.number + 1);
            };

            currentPageSpan.textContent = "Trang " + (data.number + 1) + " / " + data.totalPages;
        })
}

function getDefaultValueParam(value) {
    return value == null || value == '' ? null : value;
}
