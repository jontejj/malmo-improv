<html>
<head>
<script type="application/ld+json">
<#noautoesc>
${jsonLd}
</#noautoesc>
</script>
</head>
<body>
	<p>Dear Madam/Sir ${underName.name},<br/>
     You have preliminary reserved ${description} at ${reservationFor.name}. Your reservation number is ${reservationId}.<br/>
     What's only missing now is your payment of ${totalPrice.price} ${totalPrice.priceCurrency} via <a href="https://www.getswish.se/">Swish</a> to ${reservationFor.phoneNumber}.<br/> 
     If you're swishing from another persons number, please enter your number, ${underName.telephone}, as the reference when paying.</p>
</body>
</html>