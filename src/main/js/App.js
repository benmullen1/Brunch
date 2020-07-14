'use strict'; /* jshint -W034 */

const React = require('react');
const ReactDOM = require('react-dom');
const when = require('when');
const client = require('./client');

const follow = require('./follow'); // function to hop multiple links by "rel"

const stompClient = require('./websocket-listener');

const root = '/api';

class App extends React.Component {

	constructor(props) {
		super(props);
		this.state = {items: [], attributes: [], pageSize: 2, links: {}};
		this.updatePageSize = this.updatePageSize.bind(this);
		this.onCreate = this.onCreate.bind(this);
		this.onUpdate = this.onUpdate.bind(this);
		this.onDelete = this.onDelete.bind(this);
		this.onNavigate = this.onNavigate.bind(this);
		this.refreshCurrentPage = this.refreshCurrentPage.bind(this);
		this.refreshAndGoToLastPage = this.refreshAndGoToLastPage.bind(this);
	}

	// tag::follow-2[]
	loadFromServer(pageSize) {
		follow(client, root, [ // <1>
			{rel: 'items', params: {size: pageSize}}]
		).then(itemCollection => { // <2>
			return client({
				method: 'GET',
				path: itemCollection.entity._links.profile.href,
				headers: {'Accept': 'application/schema+json'}
			}).then(schema => {
				this.schema = schema.entity;
				this.links = itemCollection.entity._links;
				return itemCollection;
			});
		}).then(itemCollection => { // <3>
			this.page = itemCollection.entity.page;								  
			return itemCollection.entity._embedded.items.map(item =>
					client({
						method: 'GET',
						path: item._links.self.href
					})
			);
		}).then(itemPromises => { // <4>
			return when.all(itemPromises);
		}).done(items => { // <5>
			this.setState({
				page: this.page,
				items: items,													
				attributes: Object.keys(this.schema.properties),
				pageSize: pageSize,
				links: this.links
			});
		});
	}

	onCreate(newItem) {
		follow(client, root, ['items']).then(response => {
			return client({
				method: 'POST',
				path: response.entity._links.self.href,
				entity: newItem,
				headers: {'Content-Type': 'application/json'}
			})
		})
	}
	// end::create[]

	// tag::update[]
	onUpdate(item, updatedItem) {
		client({
			method: 'PUT',
			path: item.entity._links.self.href,
			entity: updatedItem,
			headers: {
				'Content-Type': 'application/json',
				'If-Match': item.headers.Etag
			}
		}).done(response => {
		}, response => {
			if (response.status.code === 412) {
				alert('DENIED: Unable to update ' +
					item.entity._links.self.href + '. Your copy is stale.');
			}
		});
	}
	// end::update[]

	// tag::delete[]
	onDelete(item) {
		client({method: 'DELETE', path: item.entity._links.self.href});
	}
	// end::delete[]

	// tag::navigate[]
	onNavigate(navUri) {
		client({
			method: 'GET',
			path: navUri
		}).then(itemCollection => {
			this.links = itemCollection.entity._links;

			return itemCollection.entity._embedded.items.map(item =>
					client({
						method: 'GET',
						path: item._links.self.href
					})
			);
		}).then(itemPromises => {
			return when.all(itemPromises);
		}).done(items => {
			this.setState({
				page: this.page,
				items: items,
				attributes: Object.keys(this.schema.properties),
				pageSize: this.state.pageSize,
				links: this.links
			});
		});
	}
	// end::navigate[]

	// tag::update-page-size[]
	updatePageSize(pageSize) {
		if (pageSize !== this.state.pageSize) {
			this.loadFromServer(pageSize);
		}
	}
	// end::update-page-size[]

// tag::websocket-handlers[]
	refreshAndGoToLastPage(message) {
		follow(client, root, [{
			rel: 'items',
			params: {size: this.state.pageSize}
		}]).done(response => {
			if (response.entity._links.last !== undefined) {
				this.onNavigate(response.entity._links.last.href);
			} else {
				this.onNavigate(response.entity._links.self.href);
			}
		})
	}

	refreshCurrentPage(message) {
		follow(client, root, [{
			rel: 'items',
			params: {
				size: this.state.pageSize,
				page: this.state.page.number
			}
		}]).then(itemCollection => {
			this.links = itemCollection.entity._links;
			this.page = itemCollection.entity.page;

			return itemCollection.entity._embedded.items.map(item => {
				return client({
					method: 'GET',
					path: item._links.self.href
				})
			});
		}).then(itemPromises => {
			return when.all(itemPromises);
		}).then(items => {
			this.setState({
				page: this.page,
				items: items,
				attributes: Object.keys(this.schema.properties),
				pageSize: this.state.pageSize,
				links: this.links
			});
		});
	}
	// end::websocket-handlers[]

	// tag::register-handlers[]
	componentDidMount() {
		this.loadFromServer(this.state.pageSize);
		stompClient.register([
			{route: '/topic/newItem', callback: this.refreshAndGoToLastPage},
			{route: '/topic/updateItem', callback: this.refreshCurrentPage},
			{route: '/topic/deleteItem', callback: this.refreshCurrentPage}
		]);
	}
	// end::register-handlers[]

	render() {
		return (
			<div>
				<CreateDialog attributes={this.state.attributes} onCreate={this.onCreate}/>
				<ItemList items={this.state.items}
							  links={this.state.links}
							  pageSize={this.state.pageSize}
							  attributes={this.state.attributes}
							  onNavigate={this.onNavigate}
							  onUpdate={this.onUpdate}
							  onDelete={this.onDelete}
							  updatePageSize={this.updatePageSize}/>
			</div>
		)
	}
}

//Fairly Customized Section
class CreateDialog extends React.Component {

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();
		const newItem = {};
		this.props.attributes.forEach(attribute => {
			newItem[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
		});
		this.props.onCreate(newItem);

		// clear out the dialog's inputs
		this.props.attributes.forEach(attribute => {
			ReactDOM.findDOMNode(this.refs[attribute]).value = '';
		});

		// Navigate away from the dialog to hide it.
		window.location = "#";
	}

	render() {
		const inputs = this.props.attributes.map(attribute =>
			<p key={attribute}>
				<input type="text" placeholder={attribute} ref={attribute} className="field"/>
			</p>
		);

		return (
			<div>
				<a className="button" href="#createItem">Create New Item</a>

				<div id="createItem" className="modalDialog">
					<div>
						<a href="#" title="Close" className="close">X</a>

						<h2>Create new item</h2>

						<form>
							{inputs}
							<button onClick={this.handleSubmit}>Create</button>
						</form>
					</div>
				</div>
			</div>
		)
	}

}

class UpdateDialog extends React.Component {

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();
		const updatedItem = {};
		this.props.attributes.forEach(attribute => {
			updatedItem[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
		});
		this.props.onUpdate(this.props.item, updatedItem);
		window.location = "#";
	}

	render() {
		const inputs = this.props.attributes.map(attribute =>
			<p key={this.props.item.entity[attribute]}>
				<input type="text" placeholder={attribute}
					   defaultValue={this.props.item.entity[attribute]}
					   ref={attribute} className="field"/>
			</p>
		);

		const dialogId = "updateItem-" + this.props.item.entity._links.self.href;

		return (
			<div key={this.props.item.entity._links.self.href}>
				<a className="button" href={"#" + dialogId}>Update</a>
				<div id={dialogId} className="modalDialog">
					<div>
						<a href="#" title="Close" className="close">X</a>

						<h2>Update an item</h2>

						<form>
							{inputs}
							<button onClick={this.handleSubmit}>Update</button>
						</form>
					</div>
				</div>
			</div>
		)
	}

};

//End Fairly Customized Section

class ItemList extends React.Component {

	constructor(props) {
		super(props);
		this.handleNavFirst = this.handleNavFirst.bind(this);
		this.handleNavPrev = this.handleNavPrev.bind(this);
		this.handleNavNext = this.handleNavNext.bind(this);
		this.handleNavLast = this.handleNavLast.bind(this);
		this.handleInput = this.handleInput.bind(this);
	}


	handleInput(e) {
		e.preventDefault();
		const pageSize = ReactDOM.findDOMNode(this.refs.pageSize).value;
		if (/^[0-9]+$/.test(pageSize)) {
			this.props.updatePageSize(pageSize);
		} else {
			ReactDOM.findDOMNode(this.refs.pageSize).value =
				pageSize.substring(0, pageSize.length - 1);
		}
	}

	handleNavFirst(e){
		e.preventDefault();
		this.props.onNavigate(this.props.links.first.href);
	}

	handleNavPrev(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.prev.href);
	}

	handleNavNext(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.next.href);
	}

	handleNavLast(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.last.href);
	}
	// End Copied Section

	// Customized
	render() {
		const items = this.props.items.map(item =>
			<Item key={item.entity._links.self.href}
					  item={item}
					  attributes={this.props.attributes}
					  onUpdate={this.props.onUpdate}
					  onDelete={this.props.onDelete}/>
		);

		const navLinks = [];
		if ("first" in this.props.links) {
			navLinks.push(<button key="first" onClick={this.handleNavFirst}>&lt;&lt;</button>);
		}
		if ("prev" in this.props.links) {
			navLinks.push(<button key="prev" onClick={this.handleNavPrev}>&lt;</button>);
		}
		if ("next" in this.props.links) {
			navLinks.push(<button key="next" onClick={this.handleNavNext}>&gt;</button>);
		}
		if ("last" in this.props.links) {
			navLinks.push(<button key="last" onClick={this.handleNavLast}>&gt;&gt;</button>);
		}

		return (
			<div>
				<label htmlFor="pageSize">Number of Records to return at once:</label>
				<input ref="pageSize" defaultValue={this.props.pageSize} onInput={this.handleInput} />
				<table>
					<tbody>
						<tr>
							<th>Item Name</th>
							<th>Description</th>
							<th>Quantity </th>
							<th></th>
							<th></th>
						</tr>
						{items}
					</tbody>
				</table>
				<div>
					{navLinks}
				</div>
			</div>
		)
	}
}


class Item extends React.Component {

	constructor(props) {
		super(props);
		this.handleDelete = this.handleDelete.bind(this);
	}

	handleDelete() {
		this.props.onDelete(this.props.item);
	}

	render() {
	    return (
		    <tr>
			    <td>{this.props.item.entity.name}</td>
			    <td>{this.props.item.entity.description}</td>
			    <td>{this.props.item.entity.quantity}</td>
			    <td>
				<UpdateDialog item={this.props.item}
				    attributes={this.props.attributes}
				    onUpdate={this.props.onUpdate}/>
			    </td>
			    <td>
				    <button onClick={this.handleDelete}>Delete</button>
			    </td>
		    </tr>
	    )
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)
