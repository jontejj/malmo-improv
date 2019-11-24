<html>
<body>
<h4>Dear ${underName.name},</h4>
<p>Malmö Improvisatorium is looking forward to seeing you at our ${reservationFor.name} show!<br/> 
Please remember to bring along your shiny smile and be ready for an evening of fun!<br/> 
<#if reservationStatus == "http://schema.org/ReservationPending">
As you plan to pay for the tickets at the entrance, please be welcome to come a few minutes early.<br/> 
If you know that you are not planning to come, please let us know (reply to this email) - improv shows are in great demand these days :)<br/> 
<#else>
Thanks for your payment through swish. Your seat will be extra warm because of this!<br/>
</#if>
<br/> 
See you there!<br/> 
<br/> 
Your Improvisers
</p>
</body>
</html>