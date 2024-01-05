/* class FlickrImageFetcher {
    constructor(apiKey) {
        this.apiKey = "8c662afa13804f7f08f850a74c26a357";
        this.endpoint = 'https://www.flickr.com/services/rest/?method=flickr.photos.search';
    }

    fetchPhotosByLocation(lat, lon, radius = 5, perPage = 10) {
        const flickrURL = `${this.endpoint}&api_key=${this.apiKey}&lat=${lat}&lon=${lon}&radius=${radius}&per_page=${perPage}&format=json&nojsoncallback=1`;

        return fetch(flickrURL)
            .then(response => response.json())
            .then(data => data.photos.photo)
            .catch(error => {
                console.error('Error fetching photos:', error);
                return [];
            });
    }

    displayPhotos(photos) {
        const photoGallery = document.getElementById('photoGallery');
        photos.forEach(photo => {
            const imageUrl = `https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg`;
            const img = document.createElement('img');
            img.src = imageUrl;
            img.alt = photo.title;
            img.classList.add('flickr-image');
            photoGallery.appendChild(img);
        });
    }
}

const flickrFetcher = new FlickrImageFetcher('8c662afa13804f7f08f850a74c26a357');

function handleFetchPhotos() {
   const latitude = 37.7128; 
    const longitude = -117.0060; 
    const radius = 50;
    const perPage = 10;
    
    flickrFetcher.fetchPhotosByLocation(latitude, longitude, radius, perPage)
        .then(photos => {
            flickrFetcher.displayPhotos(photos);
        })
        .catch(error => {
            console.error('Error fetching and displaying photos:', error);
        });
}

const fetchButton = document.getElementById('fetchButton');
fetchButton.addEventListener('click', handleFetchPhotos);
 */