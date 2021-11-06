 axios.defaults.baseURL = 'http://wxclog.com/api';
 // axios.defaults.baseURL = 'http://localhost:7777/api';
const request = {
	filter(response, reject) {
		if(response.headers['content-type'] == 'application/octet-stream'){
			return response.data;
		}
		if (response.status != 200) {
			vue.$message.error('系统错误');
		} else {
			let data = response.data;
			if (data.status != 200) {
				vue.$message.error(data.msg);
			} else {
				return data
			}
		}
	},
	fetch(url, data) {
		return new Promise((resolve, reject) => {
			axios.post(url, JSON.stringify(data), {
				headers: {
					'Content-Type': 'application/json'
				}
			}).then(response => {
				resolve(this.filter(response, reject))

			})
		})
	},
	get(url, data) {
		return new Promise((resolve, reject) => {
			axios.get(url,{params:data,responseType: "blob"}).then(response => {
				resolve(this.filter(response, reject))
			})
		})
	}
}
