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
     You have preliminary reserved ${description} at ${reservationFor.name}. Your reservation number is ${reservationNumber}.<br/>
     What's only missing now is your payment of ${totalPrice.price} ${totalPrice.priceCurrency} via <a href="https://www.getswish.se/">Swish</a> to ${reservationFor.phoneNumber}.<br/> 
     If you're swishing from another persons number, please enter your number, ${underName.telephone}, as the reference when paying.</p>
     
     <p>Details:<br/>
     <table>
     <tr><td>Date/Time:</td><td>${reservationFor.startDate}</td></tr>
     <tr><td>Location:</td><td>${reservationFor.location.name}</td></tr>
     <tr><td></td><td>${reservationFor.location.address.streetAddress}</td></tr>
     <tr><td></td><td>${reservationFor.location.address.postalCode} ${reservationFor.location.address.addressLocality} </td></tr>
     <tr><td></td><td>${reservationFor.location.address.addressRegion}, ${reservationFor.location.address.addressCountry}</td></tr>
     </table>
                
     </p>
</body>
</html>