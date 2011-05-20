/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.webservices.rest.web.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for PersonAddress, supporting standard CRUD operations
 */
@SubResource(parent = PersonResource.class, path = "addresses", parentProperty = "person")
@Handler(supports = PersonAddress.class, order = 0)
public class PersonAddressResource extends DelegatingSubResource<PersonAddress, Person, PersonResource> {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("preferred");
			description.addProperty("uri", findMethod("getUri"));
			description.addProperty("address1");
			description.addProperty("address2");
			description.addProperty("cityVillage");
			description.addProperty("stateProvince");
			description.addProperty("country");
			description.addProperty("postalCode");
			description.addProperty("countyDistrict");
			description.addProperty("address3");
			description.addProperty("address4");
			description.addProperty("address5");
			description.addProperty("address6");
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("preferred");
			description.addProperty("uri", findMethod("getUri"));
			description.addProperty("address1");
			description.addProperty("address2");
			description.addProperty("cityVillage");
			description.addProperty("stateProvince");
			description.addProperty("country");
			description.addProperty("postalCode");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("countyDistrict");
			description.addProperty("address3");
			description.addProperty("address4");
			description.addProperty("address5");
			description.addProperty("address6");
			//TODO These were introduced in 1.9, include them when we upgrade
			//description.addProperty("startDate");
			//description.addProperty("endDate");
			description.addProperty("latitude");
			description.addProperty("longitude");
			description.addProperty("auditInfo", findMethod("getAuditInfo"));
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("uri", findMethod("getUri"));
			description.addProperty("display", findMethod("getDisplayString"));
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public PersonAddress getByUniqueId(String uuid) {
		return Context.getPersonService().getPersonAddressByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void delete(PersonAddress address, String reason, RequestContext context) throws ResponseException {
		///API had no void methods as of 1.8 other
		//we should be calling voidPersonAddress that was added in 1.9
		boolean needToRemove = false;
		for (PersonAddress pa : address.getPerson().getAddresses()) {
			if (pa.equals(address)) {
				needToRemove = true;
				break;
			}
		}
		
		if (needToRemove) {
			address.setVoided(true);
			address.setVoidedBy(Context.getAuthenticatedUser());
			address.setDateVoided(new Date());
			address.setVoidReason(reason);
			Context.getPersonService().savePerson(address.getPerson());
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(PersonAddress address, RequestContext context) throws ResponseException {
		///API has no void methods as of 1.8 and earlier versios,
		//we should be calling voidPersonAddress(PersonAddress, Reason) that was added in 1.9
		boolean needToRemove = false;
		for (PersonAddress pa : address.getPerson().getAddresses()) {
			if (pa.equals(address)) {
				needToRemove = true;
				break;
			}
		}
		
		if (needToRemove) {
			address.getPerson().removeAddress(address);
			Context.getPersonService().savePerson(address.getPerson());
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#save(java.lang.Object)
	 */
	@Override
	protected PersonAddress save(PersonAddress newAddress) {
		// make sure that the name has actually been added to the person
		boolean needToAdd = true;
		for (PersonAddress pa : newAddress.getPerson().getAddresses()) {
			if (pa.equals(newAddress)) {
				needToAdd = false;
				break;
			}
		}
		
		if (needToAdd) {
			newAddress.getPerson().addAddress(newAddress);
			Context.getPersonService().savePerson(newAddress.getPerson());
		}
		return newAddress;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	protected PersonAddress newDelegate() {
		return new PersonAddress();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Person getParent(PersonAddress instance) {
		return instance.getPerson();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(PersonAddress instance, Person parent) {
		instance.setPerson(parent);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public List<PersonAddress> doGetAll(Person parent, RequestContext context) throws ResponseException {
		//We don't return voided addresses
		List<PersonAddress> nonVoidedAddresses = new ArrayList<PersonAddress>(parent.getAddresses().size());
		for (PersonAddress personAddress : parent.getAddresses()) {
			if (!personAddress.isVoided())
				nonVoidedAddresses.add(personAddress);
		}
		return nonVoidedAddresses;
	}
	
	public SimpleObject getAuditInfo(PersonAddress address) throws Exception {
		SimpleObject ret = new SimpleObject();
		ret.put("creator", ConversionUtil.getPropertyWithRepresentation(address, "creator", Representation.REF));
		ret.put("dateCreated", ConversionUtil.convertToRepresentation(address.getDateCreated(), Representation.DEFAULT));
		ret.put("voided", ConversionUtil.convertToRepresentation(address.isVoided(), Representation.DEFAULT));
		if (address.isVoided()) {
			ret.put("voidedBy", ConversionUtil.getPropertyWithRepresentation(address, "voidedBy", Representation.REF));
			ret.put("dateVoided", ConversionUtil.convertToRepresentation(address.getDateVoided(), Representation.DEFAULT));
			ret.put("voidReason", ConversionUtil.convertToRepresentation(address.getVoidReason(), Representation.DEFAULT));
		}
		return ret;
	}
	
	/**
	 * Gets the display string for a person address.
	 * 
	 * @param address the address object.
	 * @return the display string.
	 */
	public String getDisplayString(PersonAddress address) {
		return address.getAddress1();
	}
}
