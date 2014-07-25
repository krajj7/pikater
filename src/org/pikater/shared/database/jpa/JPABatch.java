package org.pikater.shared.database.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.pikater.core.ontology.subtrees.batch.batchStatuses.BatchStatuses;
import org.pikater.shared.database.jpa.status.JPABatchStatus;


@Entity
@Table(name="Batch")
@NamedQueries({
	@NamedQuery(name="Batch.getAll",query="select b from JPABatch b"),
	@NamedQuery(name="Batch.getAll.count",query="select count(b) from JPABatch b"),
	@NamedQuery(name="Batch.getAllNotStatus.count",query="select count(b) from JPABatch b where b.status <> :status"),
	@NamedQuery(name="Batch.getByID",query="select b from JPABatch b where b.id=:id"),
	@NamedQuery(name="Batch.getByStatus",query="select b from JPABatch b where b.status=:status"),
	@NamedQuery(name="Batch.getByOwner",query="select b from JPABatch b where b.owner=:owner"),
	@NamedQuery(name="Batch.getByOwner.count",query="select count(b) from JPABatch b where b.owner=:owner"),
	@NamedQuery(name="Batch.getByOwnerAndStatus.count",query="select count(b) from JPABatch b where b.owner=:owner and b.status=:status"),
	@NamedQuery(name="Batch.getByOwnerAndNotStatus.count",query="select count(b) from JPABatch b where b.owner=:owner and b.status <> :status")
})
public class JPABatch extends JPAAbstractEntity{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
	
	public int getId() {
        return id;
    }
	private String name;
	private String note;
	@Lob
	private String XML;
	@ManyToOne
	private JPAUser owner;
	private int priority;
	private int totalPriority;
	@Enumerated(EnumType.STRING)
	private JPABatchStatus status;
	
	@OneToMany(cascade=CascadeType.PERSIST)
	private List<JPAExperiment> experiments = new ArrayList<JPAExperiment>();
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	private Date finished;
	
	/**
	 * Constructor for JPA compatibility
	 */
	public JPABatch(){}
	
	/**
	 * Creates an experiment to be saved (not executed).
	 * @param name
	 * @param note
	 * @param xml
	 * @param owner
	 */
	public JPABatch(String name, String note, String xml, JPAUser owner)
	{
		this.name=name;
		this.note = note;
		this.XML = xml;
		this.owner=owner;
		this.priority = 9; // TODO: this is probably just for tests - let's have some methods to easily access the defaults


		this.totalPriority=99; // TODO: this is probably just for tests - let's have some methods to easily access the defaults
		this.created=new Date();
		this.status=JPABatchStatus.CREATED;
	}
	
	/**
	 * Creates an experiment to be saved for execution.
	 * @param name
	 * @param note
	 * @param xml
	 * @param owner
	 * @param userAssignedPriority
	 * @param computationEstimateInHours
	 * @param sendEmailAfterFinish
	 */
	public JPABatch(String name, String note, String xml, JPAUser owner, int userAssignedPriority, 
			int computationEstimateInHours, boolean sendEmailAfterFinish)
	{
		this.name=name;
		this.note = note;
		this.XML = xml;
		this.owner=owner;
		this.priority = userAssignedPriority;


		// TODO: include computation estimate and the boolean flag in the entity


		this.totalPriority=99; // TODO: this is probably just for tests
		this.created=new Date();
		this.status=JPABatchStatus.WAITING;
	}

	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return this.name;
	}
	
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

	public String getXML() {
		return XML;
	}
	public void setXML(String xML) {
		XML = xML;
	}

	public JPAUser getOwner() {
		return owner;
	}
	public void setOwner(JPAUser owner) {
		this.owner = owner;
	}

	public void setPriority(int priority){
		this.priority=priority;
	}
	public int getPriority(){
		return this.priority;
	}
	
	public void setTotalPriority(int totalPriority){
		this.totalPriority=totalPriority;
	}
	public int getTotalPriority(){
		return this.totalPriority;
	}
	
	public List<JPAExperiment> getExperiments() {
		return experiments;
	}
	public void setExperiments(List<JPAExperiment> experiments) {
		this.experiments = experiments;
	}
	public void addExperiment(JPAExperiment experiment) {
		this.experiments.add(experiment);
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getFinished() {
		return finished;
	}
	public void setFinished(Date finished) {
		this.finished = finished;
	}
	public JPABatchStatus getStatus() {
		return status;
	}
	public void setStatus(JPABatchStatus status) {
		this.status = status;
	}
	public void setStatus(String status) {

		if ( status.equals(BatchStatuses.CREATED) ) {
			this.status = JPABatchStatus.CREATED;
		} else if ( status.equals(BatchStatuses.WAITING) ) {
			this.status = JPABatchStatus.WAITING;
		} else if ( status.equals(BatchStatuses.COMPUTING) ) {
			this.status = JPABatchStatus.STARTED;
		} else if ( status.equals(BatchStatuses.FINISHED) ) {
			this.status = JPABatchStatus.FINISHED;
		} else if ( status.equals(BatchStatuses.FAILED) ) {
			this.status = JPABatchStatus.FAILED;
		} else {
			throw new IllegalArgumentException("Batch status is not valid");
		}
	}

	@Transient
	public static final String EntityName = "Batch";
	
	@Override
	public void updateValues(JPAAbstractEntity newValues) throws Exception {
		JPABatch updateValues=(JPABatch)newValues;
		this.created=updateValues.getCreated();
		this.experiments=updateValues.getExperiments();
		this.finished=updateValues.getFinished();
		this.name=updateValues.getName();
		this.note=updateValues.getNote();
		this.owner=updateValues.getOwner();
		this.priority=updateValues.getPriority();
		this.status=updateValues.getStatus();
		this.totalPriority=updateValues.getTotalPriority();
		this.XML=updateValues.getXML();
	}
}
