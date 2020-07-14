A Spring-based React application. Mainly exists to learn how to build a Spring-based React application, but also is the skeleton of other ideas we've had

Currently Spring Security is enabled, only one user exists, dev/dev

CSS was from "Skeleton CSS" project
https://github.com/dhg/Skeleton


Favicon Generated on site
https://favicon.io/favicon-generator/


Intial project setup followed this tutorial:
https://spring.io/guides/tutorials/react-and-spring-data-rest/

JSHint plugin for MVN:
https://github.com/Acosix/acosix-maven-parent/blob/master/jshint-plugin/README.md


Guidelines for adding plus/minus buttons to a field:
http://jsfiddle.net/polaszk/1oyfxoor/

here is some code I was going to use:
			    <span className="input-group">
				<input type="button" value="-" className="button-minus" data-field="quantity" onClick=>
				<input type="number" step="1" max="" value={this.props.item.quantity} name="quantity" className="quantity-field">
				<input type="button" value="+" className="button-plus" data-field="quantity">
			    </span>