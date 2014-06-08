package org.pikater.shared.database.jpa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.pikater.shared.database.jpa.security.PikaterPriviledge;
import org.pikater.shared.database.jpa.security.PikaterRole;

@Entity
@Table(name="Role")
@NamedQueries({
	@NamedQuery(name="Role.getAll",query="select r from JPARole r"),
	@NamedQuery(name="Role.getByID",query="select r from JPARole r where r.id=:id"),
	@NamedQuery(name="Role.getByPikaterRole",query="select r from JPARole r where r.role=:pRole"),
	@NamedQuery(name="Role.getByName",query="select r from JPARole r where r.name=:name")
})
public class JPARole extends JPAAbstractEntity{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
	
	public int getId() {
        return id;
    }
	@OneToMany
	private List<JPAUser> usersWithThisRole=new LinkedList<JPAUser>();
	@Column(unique=true)
	private String name;
	@Column(unique=true)
	@Enumerated(EnumType.STRING)
	private PikaterRole role;
	@OneToMany(cascade=CascadeType.MERGE)
	private List<JPAUserPriviledge> priviledges = new ArrayList<JPAUserPriviledge>(); 
	
	protected JPARole(){}
	
	public JPARole (String name,PikaterRole role){
		this.role=role;
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public PikaterRole getRole() {
		return role;
	}
	public void setRole(PikaterRole role) {
		this.role = role;
	}
	public List<JPAUser> getUsersWithThisRole() {
		return usersWithThisRole;
	}
	public void addPriviledge(JPAUserPriviledge priviledge) {
		this.priviledges.add(priviledge);
	}
	public List<JPAUserPriviledge> getPriviledges() {
		return priviledges;
	}
	public boolean isAdmin(){
		return this.role==PikaterRole.ADMIN;
	}
	public boolean isUser(){
		return this.role==PikaterRole.USER;
	}
	public boolean hasPriviledge(PikaterPriviledge priviledge) {
		for(JPAUserPriviledge userPriviledge : getPriviledges()){
			if(userPriviledge.getPriviledge()==priviledge){
				return true;
			}
		}
		return false;
	}
	@Transient
	public static final String EntityName = "Role";
	@Override
	public void updateValues(JPAAbstractEntity newValues) throws Exception {
		JPARole updatedValues=(JPARole)newValues;
		this.role=updatedValues.getRole();
		this.name=updatedValues.getName();
		this.priviledges=new ArrayList<JPAUserPriviledge>();
		for(JPAUserPriviledge priv : updatedValues.getPriviledges()){
			this.priviledges.add(priv);
		}
	}
}
