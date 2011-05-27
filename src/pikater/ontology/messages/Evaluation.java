package pikater.ontology.messages;

import jade.content.Concept;
import jade.util.leap.LinkedList;
import jade.util.leap.List;

public class Evaluation implements Concept {
	/**
	 * 
	 */
	private float maxValue = (float)Integer.MAX_VALUE;
	
	private static final long serialVersionUID = 1319671908304254420L;
	private float _error_rate = maxValue;
	private float _kappa_statistic = maxValue;
	private float _mean_absolute_error = maxValue;
	private float _root_mean_squared_error = maxValue;
	private float _relative_absolute_error = maxValue; // percent
	private float _root_relative_squared_error = maxValue; // percent
	private String _status;

	private int duration;  // integer miliseconds
	private String object_filename;
	
	//private DataInstances data_table;
	private List _labeled_data = new LinkedList(); // List of DataInstances

        private DataInstances data_table;

	private byte [] object;  // saved agent

        public void setStatus(String status) {
            _status = status;
        }

        public String getStatus() {
            return _status;
        }

        public DataInstances getData_table() {
		return data_table;
	}

	public void setData_table(DataInstances dataTable) {
		data_table = dataTable;
	}

	public void setError_rate(float error_rate) {
		if (Float.isNaN(error_rate)){
			_error_rate = maxValue;
		}
		else{
			_error_rate = error_rate;
		}
	}

	public float getError_rate() {
		return _error_rate;
	}

	public void setKappa_statistic(float kappa_statistic) {
		if (Float.isNaN(kappa_statistic)){
			_kappa_statistic = maxValue;
		}
		else{
			_kappa_statistic = kappa_statistic;
		}
	}

	public float getKappa_statistic() {
		return _kappa_statistic;
	}

	public void setMean_absolute_error(float mean_absolute_error) {
		if (Float.isNaN(mean_absolute_error)){
			_mean_absolute_error = maxValue;
		}
		else{
			_mean_absolute_error = mean_absolute_error;
		}
	}

	public float getMean_absolute_error() {
		return _mean_absolute_error;
	}

	public void setRoot_mean_squared_error(float root_mean_squared_error) {
		if (Float.isNaN(root_mean_squared_error)){
			_root_mean_squared_error = maxValue;
		}
		else{
			_root_mean_squared_error = root_mean_squared_error;
		}
	}

	public float getRoot_mean_squared_error() {
		return _root_mean_squared_error;
	}

	public void setRelative_absolute_error(float relative_absolute_error) {
		if (Float.isNaN(relative_absolute_error)){
			_relative_absolute_error = maxValue;
		}
		else{
			_relative_absolute_error = relative_absolute_error;
		}
	}

	public float getRelative_absolute_error() {
		return _relative_absolute_error;
	}

	public void setRoot_relative_squared_error(float root_relative_squared_error) {
		if (Float.isNaN(root_relative_squared_error)){
			_root_relative_squared_error = maxValue;
		}
		else{
			_root_relative_squared_error = root_relative_squared_error;
		}
	}

	public float getRoot_relative_squared_error() {
		return _root_relative_squared_error;
	}

	public List getLabeled_data() {
		return _labeled_data;
	}

	public void setLabeled_data(List labeled_data) {
		_labeled_data = labeled_data;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public void setObject_filename(String object_filename) {
		this.object_filename = object_filename;
	}

	public String getObject_filename() {
		return object_filename;
	}
	
	public void setObject(byte [] object) {
		this.object = object;
	}
	public byte [] getObject() {
		return object;
	}
}